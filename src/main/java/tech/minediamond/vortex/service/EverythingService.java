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
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.EverythingQuery;
import tech.minediamond.vortex.model.EverythingResult;
import tech.minediamond.vortex.model.SearchMode;
import tech.minediamond.vortex.service.interfaces.Everything3;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class EverythingService {

    // --- 常量 ---
    private static final int MAX_PATH = 32767;
    private static final String EVERYTHING_PATH = Paths.get("everything\\Everything64.exe").toFile().getAbsolutePath();

    private Everything3.EverythingClient client = null;

    @Inject
    public EverythingService() throws IOException, InterruptedException {
        StartEverythingInstance();
        Thread linkEverythingThread = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                for (int i = 0; i < 20; i++) {
                    client = LinkEverythingInstance();
                    if (client != null) {
                        log.info("everything连接成功");
                        return;
                    } else {
                        TimeUnit.MILLISECONDS.sleep(400);
                    }
                }
                log.warn("连接失败");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        linkEverythingThread.setName("Link Everything Instance Thread");
        linkEverythingThread.start();
    }

    private final Everything3 lib = Everything3.INSTANCE;

    public EverythingQueryBuilder QueryBuilder() {
        return new EverythingQueryBuilder(this);
    }

    public void StartEverythingInstance() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(EVERYTHING_PATH, "-instance", "vortex_backend");
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        pb.start();
    }

    public void StopEverythingInstance() throws IOException {
        if (client != null) {
            lib.Everything3_DestroyClient(client);
        }
        ProcessBuilder pb = new ProcessBuilder(EVERYTHING_PATH, "-exit", "-instance", "vortex_backend");
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        pb.start();
    }

    public Everything3.EverythingClient LinkEverythingInstance() {
        client = lib.Everything3_ConnectW(new WString("vortex_backend"));
        return client;
    }

    public List<EverythingResult> query(EverythingQuery query) {

        // 防御性检查
        if (client == null) {
            log.error("Cannot perform query: Everything service is not connected.");
            return Collections.emptyList();
        }

        Everything3.EverythingSearchState searchState = null;
        Everything3.EverythingResultList resultList = null;
        List<EverythingResult> results = new ArrayList<>();
        try {
            // 创建和配置搜索条件
            searchState = lib.Everything3_CreateSearchState();
            if (searchState == null) {
                log.error("创建搜索状态失败。");
            }
            // 设置搜索关键字
            String queryKeywords = "\"" + query.query() + "\"";
            String finalQueryString = queryKeywords;
            // 设置返回结果的最大数量
            lib.Everything3_SetSearchViewportCount(searchState, new WinDef.DWORD(200));
            //设置搜索内容
            lib.Everything3_AddSearchPropertyRequest(searchState, Everything3.PropertyType.FULL_PATH.getID()); // full_path
            lib.Everything3_AddSearchPropertyRequest(searchState, Everything3.PropertyType.SIZE.getID()); // size
            lib.Everything3_AddSearchPropertyRequest(searchState, Everything3.PropertyType.FILE_NAME.getID()); // fileName

            //生成文件夹字符串
            String pathQueryPart = "";
            if (query.targetFolders().isPresent()) {
                pathQueryPart = buildPathQueryPart(query);

            }

            //生成搜索模式字符串
            String searchModeQueryPart = "";
            if (query.searchMode().isPresent()) {
                searchModeQueryPart = buildSearchModeQueryPart(query);
            }

            finalQueryString = pathQueryPart + " "+ searchModeQueryPart + queryKeywords;

            //执行搜索
            log.info("正在执行搜索 '{}'...", query);
            log.info("搜索词: {}", finalQueryString);
            lib.Everything3_SetSearchTextW(searchState, new WString(finalQueryString));
            resultList = lib.Everything3_Search(client, searchState);
            if (resultList == null) {
                log.error("搜索执行失败。");
            }
            //遍历和处理结果
            WinDef.DWORD numResults = lib.Everything3_GetResultListViewportCount(resultList);
            log.info("找到 {} 个结果:", numResults.intValue());


            char[] buffer = new char[MAX_PATH];
            for (int i = 0; i < numResults.intValue(); i++) {
                EverythingResult everythingResult = new EverythingResult();
                //获取搜索结果的完整路径
                lib.Everything3_GetResultPropertyTextW(resultList, new WinDef.DWORD(i), Everything3.PropertyType.FULL_PATH.getID(), buffer, new WinDef.DWORD(MAX_PATH));
                String pathname = Native.toString(buffer);
                everythingResult.setFullPath(pathname);

                //获取搜索结果的名称
                lib.Everything3_GetResultPropertyTextW(resultList, new WinDef.DWORD(i), Everything3.PropertyType.FILE_NAME.getID(), buffer, new WinDef.DWORD(MAX_PATH));
                String filename = Native.toString(buffer);
                everythingResult.setFileName(filename);

                //获取搜索结果的大小(单位:Byte)
                //这里直接将返回的无符号int64转换为long，但是考虑到无符号int64达到最大位需要文件8EB以上的大小，因此直接赋值问题不大
                long size = lib.Everything3_GetResultSize(resultList, new WinDef.DWORD(i));
                everythingResult.setSize(size);

                results.add(everythingResult);
            }
        } finally {
            if (resultList != null) {
                lib.Everything3_DestroyResultList(resultList);
            }
            if (searchState != null) {
                lib.Everything3_DestroySearchState(searchState);
            }
        }
        log.info(results.toString());
        return results;
    }

    private String buildPathQueryPart(EverythingQuery query) {
        List<Path> targetFolders = query.targetFolders().orElseGet(() -> new ArrayList<>());
        String body = targetFolders.stream()
                .map(Path::toString)
                .collect(Collectors.joining("|"));
        String pathQueryPart = "ancestor:" + body;
        log.debug("pathQueryPart: {}",pathQueryPart);
        return pathQueryPart;
    }

    private String buildSearchModeQueryPart(EverythingQuery query) {
        SearchMode searchMode = query.searchMode().orElseGet(() -> SearchMode.ALL);
        log.debug("searchModeQueryPart: {}",searchMode.getQueryPrefix());
        return searchMode.getQueryPrefix();
    }

}
