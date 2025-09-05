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

/**
 * 定义 Everything 的搜索模式。
 */
public enum SearchMode {
    /**
     * 搜索文件和文件夹 (默认)。
     */
    ALL(""),

    /**
     * 仅搜索文件。
     * 这会在查询前添加 "file:" 修饰符。
     */
    FILES_ONLY("file:"),

    /**
     * 仅搜索文件夹。
     * 这会在查询前添加 "folder:" 修饰符。
     */
    FOLDERS_ONLY("folder:");

    private final String prefix;

    SearchMode(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 获取要添加到搜索查询中的前缀。
     * @return a search prefix string, for example "file:".
     */
    public String getQueryPrefix() {
        return prefix;
    }
}

