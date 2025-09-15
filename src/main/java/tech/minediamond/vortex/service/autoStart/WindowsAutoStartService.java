/*
 * Vortex
 * Copyright (C) 2025 Mine-diamond
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package tech.minediamond.vortex.service.autoStart;

import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.appConfig.AppConfig;

import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
public class WindowsAutoStartService implements IAutoStartService {

    private static final String APP_NAME = "Vortex"; // 注册表中的键名
    private static final String RUN_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";

    private final BooleanProperty autoStartEnabledProperty;
    private final AppConfig appConfig;

    @Inject
    public WindowsAutoStartService(AppConfig appConfig) {
        this.appConfig = appConfig;
        autoStartEnabledProperty = appConfig.autoStartEnabledProperty();
        log.info("WindowsAutoStartService 已初始化。开始将注册表状态与 AppConfig 同步。");

        //从注册表读取真实的、当前的自启动状态。
        boolean isEnabledInRegistry = readStateFromRegistry();
        log.info("从注册表读取到当前实际的自启动状态为: {}", isEnabledInRegistry);

        // 将 AppConfig 的状态与注册表状态同步。
        // 这可以覆盖从配置文件加载的旧状态，确保 UI 显示的是最新情况。
        if (autoStartEnabledProperty.get() != isEnabledInRegistry) {
            log.warn("AppConfig 中的状态 ({}) 与注册表中的实际状态 ({}) 不符。将以注册表为准进行同步。",
                    autoStartEnabledProperty.get(), isEnabledInRegistry);
            autoStartEnabledProperty.set(isEnabledInRegistry);
        }

        // 监听变化：当 AppConfig 中的属性变化时，自动更新注册表
        this.autoStartEnabledProperty().addListener((obs, oldVal, newVal) -> {
            log.info("检测到 autoStartEnabledProperty 自启动状态变化 -> {}。正在同步注册表...", newVal);
            synchronizeStateToRegistry(newVal);
        });
    }

    @Override
    public BooleanProperty autoStartEnabledProperty() {
        return autoStartEnabledProperty;
    }

    /**
     * 新增方法：从注册表读取当前的自启动状态。
     *
     * @return 如果注册表中存在应用的自启动项，则返回 true；否则返回 false。
     */
    private boolean readStateFromRegistry() {
        try {
            // 使用 "reg query" 命令来检查键是否存在
            ProcessBuilder pb = new ProcessBuilder("reg", "query", RUN_KEY, "/v", APP_NAME);
            Process process = pb.start();
            // 如果命令成功执行（退出码为 0），意味着键存在。
            // 如果键不存在，命令会失败，退出码为 1。
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            log.error("检查注册表自启动状态时发生异常。将默认状态视为 '禁用'。", e);
            // 发生错误时，返回 false 是最安全的选择。
            return false;
        }
    }

    /**
     * 根据目标状态，决定是启用还是禁用自启动。
     *
     * @param shouldBeEnabled true 表示应启用自启动，false 表示应禁用。
     */
    private void synchronizeStateToRegistry(boolean shouldBeEnabled) {
        if (shouldBeEnabled) {
            performEnable();
        } else {
            performDisable();
        }
    }

    /**
     * 执行启用自启动的操作，即向注册表添加键值。
     */
    private void performEnable() {
        try {
            String exePath = getExecutablePath();
            if (exePath == null) {
                log.error("无法找到应用的执行路径，启用自启动失败。");
                // 自动将配置改回去，因为操作失败了
                appConfig.setAutoStartEnabledProperty(false);
                return;
            }
            // 格式化命令，确保路径被引号包裹，内部引号加反斜杠，并添加 --autostart 参数
            String valueData = String.format("\"\\\"%s\\\" --autostart\"", exePath);
            ProcessBuilder pb = new ProcessBuilder(
                    "reg", "add", RUN_KEY, "/v", APP_NAME, "/t", "REG_SZ", "/d", valueData, "/f"
            );
            executeCommand(pb, "启用");
        } catch (Exception e) {
            log.error("启用开机自启动时发生严重异常。", e);
            // 发生异常时，也应该将配置改回去
            appConfig.setAutoStartEnabledProperty(false);
        }
    }

    /**
     * 执行禁用自启动的操作，即从注册表删除键值。
     */
    private void performDisable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("reg", "delete", RUN_KEY, "/v", APP_NAME, "/f");
            executeCommand(pb, "禁用");
        } catch (Exception e) {
            log.error("禁用开机自启动时发生严重异常。", e);
        }
    }

    /**
     * 执行具体的命令行进程。
     *
     * @param pb         配置好的 ProcessBuilder
     * @param actionName 操作名称，用于日志记录（如 "启用" 或 "禁用"）
     */
    private void executeCommand(ProcessBuilder pb, String actionName) throws InterruptedException, IOException {
        log.info("执行注册表命令: {}", String.join(" ", pb.command()));
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            log.info("成功{}开机自启动。", actionName);
        } else {
            // 对于 "delete" 操作，如果键不存在，也可能返回非 0 退出码，这通常是可接受的
            log.warn("注册表命令执行完毕，但退出码为: {}。这在某些情况下是正常的（例如，删除一个不存在的键）。", exitCode);
        }
    }

    /**
     * 获取当前运行的应用程序的可执行文件路径（.exe 或 .jar）。
     *
     * @return 可执行文件的绝对路径，如果失败则返回 null。
     */
    private String getExecutablePath() {
        try {
            String programPath = System.getProperty("jpackage.app-path");
            log.info("程序路径：{}", programPath);
            return Paths.get(programPath).toFile().getAbsolutePath();

        } catch (Exception e) {
            log.error("获取可执行文件路径时发生 URI 语法错误。", e);
            return null;
        }
    }
}
