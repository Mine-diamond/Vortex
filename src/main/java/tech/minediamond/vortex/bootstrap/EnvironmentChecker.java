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

package tech.minediamond.vortex.bootstrap;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.File;
import java.nio.file.Paths;

/**
 * 对运行环境进行检查的类，通过check()方法调用，会检测操作系统，无头环境和Everything程序，若不满足条件会直接结束程序并返回false
 */
@Slf4j
public class EnvironmentChecker {
    public static boolean check() {
        try {
            return checkSystem()
                    && checkHeadless()
                    && checkEverythingExecutableExists();
        } catch (Exception e) {
            log.error("在检查环境时出错: {}", e.getMessage(), e);
            Platform.exit();
            return false;
        }
    }

    //以下的check方法均为返回true为通过,false为不通过
    private static boolean checkSystem() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            systemAlertStop();
            log.error("系统不支持");
            Platform.exit();
            return false;
        }
        return true;
    }

    private static boolean checkHeadless() {
        if (GraphicsEnvironment.isHeadless()) {
            log.error("环境为无头环境");
            System.err.println("Error: This is a GUI application and cannot be run in a headless environment. Please run it on a system with a graphical user interface.");
            Platform.exit();
            return false;
        }
        return true;
    }

    private static boolean checkEverythingExecutableExists() {
        final String EVERYTHING_PATH = Paths.get("everything\\Everything64.exe").toFile().getAbsolutePath();
        File file = new File(EVERYTHING_PATH);
        if (!file.exists()) {
            everythingAlertStop();
            log.error("引索程序未找到，期待的位置：{}", EVERYTHING_PATH);
            Platform.exit();
            return false;
        }
        return true;
    }

    private static void systemAlertStop() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("error");
        alert.setHeaderText("System not supported");
        alert.setContentText("Vortex only supports running on Windows systems\nIt does not support running on Linux, Mac, or other\nsystems");
        alert.showAndWait();
    }

    private static void everythingAlertStop(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("error");
        alert.setHeaderText("File indexing service not found");
        alert.setContentText("Try redownload and reinstall Vortex to resolve the issue.");
        alert.showAndWait();
    }
}
