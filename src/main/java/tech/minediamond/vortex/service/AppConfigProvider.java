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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.AppConfig;

import java.io.File;
import java.io.IOException;

/**
 * 加载并提供AppConfig
 * @see AppConfigService
 */
@Slf4j
@Singleton
public class AppConfigProvider implements Provider<AppConfig> {

    private static final File CONFIG_FILE = new File("config.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // get() 方法是创建 AppConfig 实例的入口
    @Override
    public AppConfig get() {
        log.info("正在加载应用配置");
        try {
            return CONFIG_FILE.exists()
                    ? MAPPER.readValue(CONFIG_FILE, AppConfig.class)
                    : new AppConfig();
        } catch (IOException e) {
            log.error("配置读取失败，使用默认值: {}", e.getMessage());
            return new AppConfig();
        }
    }
}
