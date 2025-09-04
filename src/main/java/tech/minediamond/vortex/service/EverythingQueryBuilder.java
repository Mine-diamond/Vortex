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

import tech.minediamond.vortex.model.EverythingQuery;
import tech.minediamond.vortex.model.EverythingResult;
import tech.minediamond.vortex.model.RequestFlag;
import tech.minediamond.vortex.model.SearchMode;

import java.nio.file.Path;
import java.util.*;

public class EverythingQueryBuilder {

    EverythingService everythingService;

    // 状态字段保持不变
    private String query;
    private SearchMode searchMode = SearchMode.ALL;
    private List<Path> targetFolders = Collections.emptyList();

    // 构造函数接收 Service
    public EverythingQueryBuilder(EverythingService everythingService) {
        this.everythingService = everythingService;
    }

    // Fluent API 方法保持不变
    public EverythingQueryBuilder searchFor(String query) { this.query = query; return this; }
    public EverythingQueryBuilder inFolders(List<Path> folders) { this.targetFolders = folders; return this; }
    public EverythingQueryBuilder mode(SearchMode mode) { this.searchMode = mode; return this; }

    // 新增一个 build 方法，用于创建配置对象
    public EverythingQuery build() {
        return new EverythingQuery(query, Optional.ofNullable(searchMode), Optional.ofNullable(targetFolders));
    }

    public List<EverythingResult> query() {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return everythingService.query(build());
    }
}
