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
import tech.minediamond.vortex.model.SearchMode;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 文件搜索查询构建器，提供流式API构建查询请求。
 * <p>
 * 该构建器支持以下查询配置：
 * <ul>
 *     <li>{@link #searchFor(String)} - 设置搜索关键词</li>
 *     <li>{@link #inFolders(List)} - 指定在哪些文件夹中执行搜索</li>
 *     <li>{@link #mode(SearchMode)} - 设置搜索模式（文件/文件夹/全部）</li>
 * </ul>
 * <p>
 * 构建完成后，通过{@code query()}发起请求，并返回一个{@code List<EverythingResult>}
 *
 * @see EverythingService
 */
public class EverythingQueryBuilder {

    EverythingService everythingService;

    private String query;
    private SearchMode searchMode = SearchMode.ALL;
    private List<Path> targetFolders = Collections.emptyList();

    // 构造函数接收 Service
    public EverythingQueryBuilder(EverythingService everythingService) {
        this.everythingService = everythingService;
    }

    // Fluent API
    /**
     * 设置搜索关键词。
     *
     * @param query 要搜索的关键词
     * @return 当前构建器实例，用于链式调用
     */
    public EverythingQueryBuilder searchFor(String query) {
        this.query = query;
        return this;
    }

    /**
     * 设置搜索范围的文件夹列表。
     *
     * @param folders 要搜索的文件夹路径列表
     * @return 当前构建器实例，用于链式调用
     */
    public EverythingQueryBuilder inFolders(List<Path> folders) {
        this.targetFolders = (folders == null) ? Collections.emptyList() : folders;
        return this;

    }

    /**
     * 设置搜索模式（文件/文件夹/全部）。
     *
     * @param mode 搜索模式，见{@link SearchMode}
     * @return 当前构建器实例，用于链式调用
     */
    public EverythingQueryBuilder mode(SearchMode mode) {
        this.searchMode = (mode == null) ? SearchMode.ALL : mode;
        return this;
    }

    /**
     * 构建查询参数对象。
     *
     * @return 封装了查询参数的{@link EverythingQuery}对象
     */
    private EverythingQuery build() {
        return new EverythingQuery(query, Optional.ofNullable(searchMode), Optional.ofNullable(targetFolders));
    }

    /**
     * 执行搜索查询并返回结果。
     * <p>
     * 如果查询关键词为空或仅包含空白字符，则返回空列表。
     *
     * @return 搜索结果列表
     */
    public List<EverythingResult> query() {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return everythingService.query(build());
    }
}
