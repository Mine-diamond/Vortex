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

package tech.minediamond.vortex.service.ui;

import com.google.inject.Inject;
import com.jthemedetecor.OsThemeDetector;
import javafx.application.Platform;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.appConfig.AppConfig;
import tech.minediamond.vortex.model.ui.Theme;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * 主题服务类，负责管理应用程序的浅色/深色主题切换,
 * 支持自动检测系统主题并实时切换应用主题
 */
@Slf4j
public class ThemeService {

    private static final String CSS_PATH;
    private static final String LIGHT_CSS_PATH;
    private static final String DARK_CSS_PATH;

    static {
        CSS_PATH = getResourcePath("/tech/minediamond/vortex/css/fluent-style.css");
        LIGHT_CSS_PATH = getResourcePath("/tech/minediamond/vortex/css/light-theme.css");
        DARK_CSS_PATH = getResourcePath("/tech/minediamond/vortex/css/dark-theme.css");
    }

    private final AppConfig config;
    private final OsThemeDetector detector;

    private Theme currentThemeType;
    private final List<Scene> scenes = Collections.synchronizedList(new ArrayList<>());

    // 系统主题自动切换监听器
    private final Consumer<Boolean> autoSwitcher = new Consumer<>() {
        @Override
        public void accept(Boolean isDark) {
            Platform.runLater(() -> {
                if (config.getTheme() != Theme.AUTO) {
                    return;
                }
                if (isDark && currentThemeType == Theme.LIGHT) {
                    themeToDark();
                }
                if (!isDark && currentThemeType == Theme.DARK) {
                    themeToLight();
                }
            });
        }
    };

    /**
     * 获取资源文件的URL路径
     *
     * @param resource 资源文件路径
     * @return 资源的完整URL路径，如果资源不存在则返回空字符串
     */
    private static String getResourcePath(String resource) {
        URL url = ThemeService.class.getResource(resource);
        if (url == null) {
            log.error("资源未找到: {}", resource);
            return "";
        }
        return url.toExternalForm();
    }

    @Inject
    public ThemeService(AppConfig config) {
        this.config = config;
        this.detector = OsThemeDetector.getDetector();

        config.themeProperty().addListener((observable, oldValue, newValue) -> {
            applyTheme(newValue);
        });
        detector.registerListener(autoSwitcher);

    }

    /**
     * 注册场景到主题服务
     * 为场景添加基础样式表并应用当前主题
     *
     * @param scene 需要注册的JavaFX场景对象
     */
    public void registerScene(Scene scene) {

        if (scenes.contains(scene)) {
            log.warn("{}已经被初始化了", scene);
            return;
        } else {
            scenes.add(scene);
        }
        scene.getStylesheets().add(CSS_PATH);

        applyTheme(config.getTheme());
    }

    /**
     * 应用指定主题到所有已注册场景
     *
     * @param theme 要应用的主题枚举值
     */
    private void applyTheme(Theme theme) {
        Theme themeToApply = theme;
        if (themeToApply == Theme.AUTO) {
            themeToApply = detector.isDark() ? Theme.DARK : Theme.LIGHT;
        }
        if (themeToApply == Theme.LIGHT) {
            themeToLight();
        } else {
            themeToDark();
        }
    }

    /**
     * 切换到浅色主题
     * 移除深色样式表，添加浅色样式表
     */
    private void themeToLight() {

        if (currentThemeType == Theme.LIGHT) {
            return;
        }
        synchronized (scenes) {
            for (Scene scene : scenes) {
                scene.getStylesheets().remove(DARK_CSS_PATH);
                if (!scene.getStylesheets().contains(LIGHT_CSS_PATH)) {
                    scene.getStylesheets().add(LIGHT_CSS_PATH);
                }
            }
        }
        currentThemeType = Theme.LIGHT;
        log.info("主题变为浅色主题");
    }

    /**
     * 切换到深色主题
     * 移除浅色样式表，添加深色样式表
     */
    private void themeToDark() {
        if (currentThemeType == Theme.DARK) {
            return;
        }
        synchronized (scenes) {
            for (Scene scene : scenes) {
                scene.getStylesheets().remove(LIGHT_CSS_PATH);
                if (!scene.getStylesheets().contains(DARK_CSS_PATH)) {
                    scene.getStylesheets().add(DARK_CSS_PATH);
                }
            }
        }
        currentThemeType = Theme.DARK;
        log.info("主题变为深色主题");
    }

    /**
     * 取消注册场景
     * 从主题服务中移除指定场景
     *
     * @param scene 需要取消注册的JavaFX场景对象
     */
    public void unregisterScene(Scene scene) {
        scenes.remove(scene);
    }

    /**
     * 清理资源
     * 清空已注册的场景列表
     */
    public void cleanup() {
        synchronized (scenes) {
            scenes.clear();
        }
    }
}
