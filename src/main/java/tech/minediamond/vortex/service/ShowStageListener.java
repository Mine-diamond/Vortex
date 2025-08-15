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

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import javafx.application.Platform;
import javafx.stage.Stage;
import tech.minediamond.vortex.model.AppConfig;

public class ShowStageListener implements NativeKeyListener {
    private Stage stage; // 持有对主窗口的引用
    private WindowAnimator windowAnimator;
    private AppConfig appConfig;

    @Inject
    public ShowStageListener(@Assisted Stage stage, WindowAnimator windowAnimator, AppConfig appConfig) {
        this.stage = stage;
        this.windowAnimator = windowAnimator;
        this.appConfig = appConfig;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e){


        if(e.getKeyCode() == NativeKeyEvent.VC_SPACE && ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0)){

            Platform.runLater(() -> {
                if (stage.isShowing()) {
                    windowAnimator.hideWindow(stage);
                } else {
                    windowAnimator.showWindow(stage, appConfig.getIfCenterOnScreen());
                }
            });

        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {

    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {

    }
}
