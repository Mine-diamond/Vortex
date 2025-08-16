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
    zh_CN("zh", "CN","lang.zh_CN"),zh_TW("zh","TW","lang.zh_TW"), en("en","lang.en"), auto(null,"lang.auto");

    private final Locale locale;
    @Getter
    private final String i18nKey;

    // 构造函数
    SupportedLocales(String language, String country, String i18nKey) {
        this.locale = new Locale(language, country);
        this.i18nKey = i18nKey;
    }

    SupportedLocales(String language, String i18nKey) {
        if (language != null) {
            this.locale = new Locale(language);
        } else {
            this.locale = null; // auto 的情况
        }
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