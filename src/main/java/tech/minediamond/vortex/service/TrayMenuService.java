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

package tech.minediamond.vortex.service;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import tech.minediamond.vortex.model.AppConfig;

/**
 * 应用托盘服务类，初始化是应确保{@link StageProvider} 初始化完成
 */
@Singleton
public class TrayMenuService {

    private static final String X16_ICON_PATH = "/images/app_icon_x16.png";

    private final AppConfig appConfig;
    private final StageProvider stageProvider;
    private final WindowAnimator windowAnimator;

    private final Stage stage;
    private final FXTrayIcon icon;

    @Inject
    public TrayMenuService(AppConfig appConfig, StageProvider stageProvider, WindowAnimator windowAnimator) {
        this.appConfig = appConfig;
        this.stageProvider = stageProvider;
        this.windowAnimator = windowAnimator;

        this.stage = stageProvider.getStage();
        icon = new FXTrayIcon(stage, getClass().getResource(X16_ICON_PATH));
        setupTrayMenu();
    }

    /**
     * 对托盘的初始化操作
     */
    private void setupTrayMenu() {
        icon.setTooltip("Vortex 快捷面板");

        MenuItem pinItem = new MenuItem();
        pinItem.textProperty().bind(Bindings.when(appConfig.autoCloseOnFocusLossProperty()).then("Close when loses focus").otherwise("Don't Close when loses focus"));
        pinItem.setOnAction(event -> {
            appConfig.setAutoCloseOnFocusLoss(!appConfig.getAutoCloseOnFocusLoss());
        });

        MenuItem openItem = new MenuItem();
        openItem.setOnAction(event -> {
            if (stage.isShowing()) {
                windowAnimator.hideWindow(stage);
            } else {
                windowAnimator.showWindow(stage, appConfig.getIfCenterOnScreen());
            }
        });

        openItem.textProperty().bind(Bindings.when(stage.showingProperty()).then("close window").otherwise("open window"));
        MenuItem exitItem = new MenuItem("exit");
        exitItem.setOnAction(event -> {
            if (stage.isShowing()) {
                windowAnimator.hideWindow(stage, Platform::exit);
            } else {
                Platform.exit();
            }
        });

        icon.addMenuItem(pinItem);
        icon.addMenuItem(new MenuItem("-"));
        icon.addMenuItem(openItem);
        icon.addMenuItem(exitItem);
        icon.show();
    }

    /**
     * 应用此方法关闭托盘
     */
    public void closeTrayMenu() {
        icon.hide();
    }

}
