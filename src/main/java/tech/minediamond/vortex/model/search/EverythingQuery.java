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

package tech.minediamond.vortex.model.search;

import tech.minediamond.vortex.service.search.EverythingQueryBuilder;
import tech.minediamond.vortex.service.search.EverythingService;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * 作为{@link EverythingService#query(EverythingQuery)}的传入参数，由 {@link EverythingQueryBuilder}构造
 *
 * @param query 搜索关键词
 * @param searchMode 搜索模式（文件/文件夹/全部）
 * @param targetFolders 在哪个文件夹搜索
 */
public record EverythingQuery(
        String query,
        Optional<SearchMode> searchMode,
        Optional<List<Path>> targetFolders) {

}
