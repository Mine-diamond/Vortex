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

package tech.minediamond.vortex.service.search;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.fileData.FileType;
import tech.minediamond.vortex.model.search.EverythingQuery;
import tech.minediamond.vortex.model.fileData.FileData;
import tech.minediamond.vortex.model.search.SearchMode;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 提供与Everything搜索引擎的交互功能的服务类。
 * <p>
 * 该服务负责启动、连接和查询Everything实例，使应用能够利用Everything的高效文件搜索能力。
 * 服务会在初始化时自动启动并连接到一个独立的Everything实例。
 * <p>
 * 使用示例：
 * <pre>
 * EverythingService service = injector.getInstance(EverythingServiceTest.class);
 * List&lt;FileData&gt; results = service.QueryBuilder()
 *     .searchFor("document")
 *     .inFolders(List.of(Path.of("C:/Users")))
 *     .mode(SearchMode.FILES_ONLY)
 *     .query();
 * </pre>
 *
 * @see EverythingQueryBuilder
 * @see FileData
 */
@Singleton
@Slf4j
public class EverythingService {

    private static final int MAX_PATH = 32767;
    private static final String EVERYTHING_PATH = Paths.get("everything\\Everything64.exe").toFile().getAbsolutePath();

    private Everything3.EverythingClient client = null;
    private final Everything3 lib = Everything3.INSTANCE;
    Thread linkEverythingThread;

    @Inject
    public EverythingService() throws IOException, InterruptedException {
        StartEverythingInstance();
        linkEverythingThread = new Thread(() -> {
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
                log.warn("linkEverythingThread被中断");
            }
        });
        linkEverythingThread.setName("Link Everything Instance Thread");
        linkEverythingThread.start();
    }


    /**
     * 创建并返回一个查询构建器，用于构建搜索请求。
     *
     * @return 一个新的EverythingQueryBuilder实例，支持流式API
     */
    public EverythingQueryBuilder QueryBuilder() {
        return new EverythingQueryBuilder(this);
    }

    /**
     * 启动一个独立的Everything实例。
     * <p>
     * 该实例使用"vortex_backend"作为唯一标识符。
     *
     * @throws IOException 启动进程失败时抛出
     */
    public void StartEverythingInstance() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(EVERYTHING_PATH, "-instance", "vortex_backend");
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        pb.start();
    }

    /**
     * 停止并关闭Everything实例。
     * <p>
     * 如果已连接到实例，会先销毁客户端连接，然后关闭实例。
     *
     * @throws IOException 执行关闭命令失败时抛出
     */
    public void stopEverythingInstance() throws IOException {
        if (client != null) {
            lib.Everything3_DestroyClient(client);
        }
        if (linkEverythingThread != null && linkEverythingThread.isAlive()) {
            linkEverythingThread.interrupt();
        }
        ProcessBuilder pb = new ProcessBuilder(EVERYTHING_PATH, "-exit", "-instance", "vortex_backend");
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        pb.start();
    }

    /**
     * 连接到正在运行的Everything实例。
     *
     * @return Everything客户端句柄，连接失败时返回null
     */
    public Everything3.EverythingClient LinkEverythingInstance() {
        client = lib.Everything3_ConnectW(new WString("vortex_backend"));
        return client;
    }

    public List<FileData> query(EverythingQuery query) {

        // 防御性检查
        if (client == null) {
            log.error("无法执行查询：Everything 服务未连接。");
            return Collections.emptyList();
        }

        if (!lib.Everything3_IsDBLoaded(client)) {
            log.error("无法执行查询：Everything数据库未加载。");
            return Collections.emptyList();
        }

        Everything3.EverythingSearchState searchState = null;
        Everything3.EverythingResultList resultList = null;
        List<FileData> results = new ArrayList<>();
        try {
            // 创建和配置搜索条件
            searchState = lib.Everything3_CreateSearchState();
            if (searchState == null) {
                log.error("无法执行查询：创建搜索失败。");
            }
            // 设置搜索关键字
            String finalQueryString;
            // 设置返回结果的最大数量
            lib.Everything3_SetSearchViewportCount(searchState, new WinDef.DWORD(200));
            //设置搜索内容
            lib.Everything3_AddSearchPropertyRequest(searchState, Everything3.PropertyType.FULL_PATH.getID());
            lib.Everything3_AddSearchPropertyRequest(searchState, Everything3.PropertyType.SIZE.getID());
            lib.Everything3_AddSearchPropertyRequest(searchState, Everything3.PropertyType.FILE_NAME.getID());
            lib.Everything3_AddSearchPropertyRequest(searchState, Everything3.PropertyType.IS_FOLDER.getID());

            //生成搜索词字符串
            String queryKeywords = "\"" + query.query() + "\"";
            //去除部分关键字
            List<String> forbiddenChar = Arrays.asList("\"", "*", "?", "<", ">", "|");
            String regex = forbiddenChar.stream()
                    .map(Pattern::quote) // 使用Pattern.quote处理特殊字符
                    .collect(Collectors.joining("|"));

            queryKeywords = queryKeywords.replaceAll(regex, "");

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
                FileData fileData = new FileData();
                //获取搜索结果的完整路径
                lib.Everything3_GetResultPropertyTextW(resultList, new WinDef.DWORD(i), Everything3.PropertyType.FULL_PATH.getID(), buffer, new WinDef.DWORD(MAX_PATH));
                String pathname = Native.toString(buffer);
                fileData.setFullPath(pathname);

                //获取搜索结果的名称
                lib.Everything3_GetResultPropertyTextW(resultList, new WinDef.DWORD(i), Everything3.PropertyType.FILE_NAME.getID(), buffer, new WinDef.DWORD(MAX_PATH));
                String filename = Native.toString(buffer);
                fileData.setFileName(filename);

                //获取搜索结果的大小(单位:Byte)
                //这里直接将返回的无符号int64转换为long，但是考虑到无符号int64达到最大位需要文件8EB以上的大小，因此直接赋值问题不大
                long size = lib.Everything3_GetResultSize(resultList, new WinDef.DWORD(i));
                fileData.setSize(size);

                //获取文件的类型
                byte type = lib.Everything3_GetResultPropertyBYTE(resultList, new WinDef.DWORD(i), Everything3.PropertyType.IS_FOLDER.getID());
                int intType = type & 0xFF;
                if (intType != 0) {
                    fileData.setType(FileType.FOLDER);
                } else {
                    fileData.setType(FileType.FILE);
                }

                results.add(fileData);
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

    //构建搜索路径部分字符串
    private String buildPathQueryPart(EverythingQuery query) {
        List<Path> targetFolders = query.targetFolders().orElseGet(ArrayList::new);
        String body = targetFolders.stream()
                .map(Path::toString)
                .collect(Collectors.joining("|"));
        String pathQueryPart = "ancestor:" + body;
        log.debug("搜索路径关键词: {}",pathQueryPart);
        return pathQueryPart;
    }

    //构建搜索描述部分字符串
    private String buildSearchModeQueryPart(EverythingQuery query) {
        SearchMode searchMode = query.searchMode().orElse(SearchMode.ALL);
        log.debug("searchMode关键词: {}",searchMode.getQueryPrefix());
        return searchMode.getQueryPrefix();
    }

}
