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

package tech.minediamond.vortex.model;

import lombok.Getter;

import java.util.Locale;

/**
 * 定义了受支持的语言，其中{@code auto}为选择系统语言，使用{@link #toLocale()}转换为{@code Locale}对象
 */
public enum SupportedLocales {
    ZH_CN(Locale.CHINA,"lang.zh_CN"),ZH_TW(Locale.TAIWAN,"lang.zh_TW"), EN(Locale.ENGLISH,"lang.en"),
    AUTO(null,"lang.auto") {
        @Override
        public Locale toLocale() {
            Locale locale = Locale.getDefault();
            if ("zh".equals(locale.getLanguage())) {
                String script = locale.getScript();
                // 如果脚本是繁体中文 (Hant)，则映射到台湾地区
                if ("Hant".equalsIgnoreCase(script)) {
                    return Locale.TAIWAN;
                }
                // 如果脚本是简体中文 (Hans) 或没有指定脚本（通常也应视为简体），则映射到中国大陆地区
                // 这样做可以覆盖 zh_SG (新加坡) 等情况
                if ("Hans".equalsIgnoreCase(script) || script.isEmpty()) {
                    return Locale.CHINA;
                }
            }
            return locale;
        }
    };

    private final Locale locale;
    @Getter
    private final String i18nKey;

    // 构造函数
    SupportedLocales(Locale locale, String i18nKey) {
        this.locale = locale;
        this.i18nKey = i18nKey;
    }

    /**
     * 获取此枚举对应的 Locale 对象。
     * 对于 auto，返回系统默认的 Locale。
     *
     * @return a Locale instance.
     */
    public Locale toLocale() {
        return this.locale == null ? Locale.getDefault() : this.locale;
    }
}