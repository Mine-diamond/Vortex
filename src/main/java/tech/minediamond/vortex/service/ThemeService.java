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

import com.google.inject.Inject;
import javafx.scene.Scene;
import tech.minediamond.vortex.model.AppConfig;

import java.util.Objects;

public class ThemeService {

    final String cssPath = Objects.requireNonNull(getClass().getResource("/tech/minediamond/vortex/css/fluent-style.css")).toExternalForm();
    final String lightCssPath = Objects.requireNonNull(getClass().getResource("/tech/minediamond/vortex/css/light-theme.css")).toExternalForm();
    final String darkCssPath = Objects.requireNonNull(getClass().getResource("/tech/minediamond/vortex/css/dark-theme.css")).toExternalForm();

    private AppConfig config;

    @Inject
    public ThemeService(AppConfig config) {
        this.config = config;
    }

    public void setupToggleTheme(Scene scene) {
        scene.getStylesheets().add(cssPath);
        switch (config.getTheme()) {
            case DARK -> {
                scene.getStylesheets().add(darkCssPath);
            }
            case LIGHT -> {
                scene.getStylesheets().add(lightCssPath);
            }
        }
        config.themeProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case DARK -> {
                    scene.getStylesheets().remove(lightCssPath);
                    scene.getStylesheets().add(darkCssPath);
                }
                case LIGHT -> {
                    scene.getStylesheets().remove(darkCssPath);
                    scene.getStylesheets().add(lightCssPath);
                }
            }
        });
    }

    public void initialize(Scene scene) {
        setupToggleTheme(scene);
    }
}
