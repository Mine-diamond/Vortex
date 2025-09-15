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

package tech.minediamond.vortex.service.appConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.appConfig.AppConfig;

import java.io.File;
import java.io.IOException;

/**
 * 为AppConfig提供保存服务
 * @see AppConfigProvider
 */
@Slf4j
@Singleton
public class AppConfigService {
    private static final File CONFIG_FILE = new File("config.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AppConfig appConfig;

    @Inject
    public AppConfigService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public void save() {
        try {
            MAPPER.writeValue(CONFIG_FILE, appConfig);
            log.info("保存配置成功");
        } catch (IOException e) {
            log.error("保存配置失败: {}", e.getMessage());
        }
    }
}
