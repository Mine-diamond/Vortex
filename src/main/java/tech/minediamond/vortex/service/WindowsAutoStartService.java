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
import javafx.beans.property.BooleanProperty;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.AppConfig;
import tech.minediamond.vortex.service.interfaces.IAutoStartService;

@Slf4j
public class WindowsAutoStartService implements IAutoStartService {

    private final BooleanProperty autoStartEnabledProperty;

    @Inject
    public WindowsAutoStartService(AppConfig appConfig) {
        autoStartEnabledProperty = appConfig.autoStartEnabledProperty();

        autoStartEnabledProperty.addListener((obs, oldValue, newValue) -> {
            log.info("开机自启动启用: {}", newValue);
        });
    }

    @Override
    public BooleanProperty autoStartEnabledProperty() {
        return autoStartEnabledProperty;
    }
}
