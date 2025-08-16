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
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.AppConfig;

import java.text.MessageFormat;
import java.util.*;

/**
 * 获取多语言文本的服务,会获取{@link AppConfig#getUserLocales()} 选定的语言的文本
 * <p>
 * 通过{@link #getString(String)} 和 {@link #getString(String, Object...)} 获取文本
 * <p>
 * 不支持程序运行中切换语言，修改语言需应用重启
 */
@Singleton
@Slf4j
public class I18nService {

    private AppConfig appConfig;

    private Locale locale;
    @Getter
    private ResourceBundle resourceBundle;

    private final String resourceBundleBaseName = "lang.I18n";

    @Inject
    public I18nService(AppConfig appConfig) {
        this.appConfig = appConfig;
        locale = appConfig.getUserLocales().toLocale();
        resourceBundle = ResourceBundle.getBundle(resourceBundleBaseName,this.locale);
    }

    /**
     * 获取文本
     * @param key 资源文件中的键
     * @param args 替换占位符的参数
     * @return 格式化后的字符串，如果找不到key则返回key本身
     */
    public String getString(String key,Object... args) {
        try {
            return MessageFormat.format(resourceBundle.getString(key), args);
        }  catch (MissingResourceException e) {
            log.error("Cannot find key {} in resource bundle", key, e);
            return "[Missing: ]" + key;
        } catch (IllegalFormatException e) {
            log.error("Illegal format string, key={}, args={}", key, Arrays.toString(args), e);
            return "[Fail: ]" + key;
        }

    }

    /**
     * 获取文本
     * @param key 资源文件中的键
     * @return 对应key的字符串
     */
    public String getString(String key) {
        return getString(key, new Object[0]);
    }

}
