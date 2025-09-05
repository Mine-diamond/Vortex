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

package tech.minediamond.vortex.service.interfaces;

import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

import lombok.Getter;

/**
 * Everything SDK v3 (适用于 Everything 1.5) 的 JNA 接口。
 * 这个接口将 Everything3_x64.dll 中的 C 函数映射到 Java 方法。
 * 为了与 Java 的 Unicode 兼容，它主要使用宽字符（W）版本的函数。
 */
public interface Everything3 extends StdCallLibrary {

    /**
     * 定义了everything3搜索需要的属性值
     */
    @Getter
    enum PropertyType {
        FILE_NAME(0),
        SIZE(2),
        FULL_PATH(240);

        final WinDef.DWORD ID;

        PropertyType(long ID) {
            this.ID = new WinDef.DWORD(ID);
        }
    }

    // 加载 DLL
    Everything3 INSTANCE = Native.load("Everything3_x64", Everything3.class);

    // --- 常量 ---
    int MAX_PATH = 260; // 从 WinDef.MAX_PATH 也可以获取

    // 类型安全的指针定义
    // 使用 PointerType 的子类可以防止将一种类型的句柄错误地传递给需要另一种类型句柄的函数。
    class EverythingClient extends PointerType {
    }

    class EverythingSearchState extends PointerType {
    }

    class EverythingResultList extends PointerType {
    }

    // 函数映射

    // === 1. 连接与客户端管理 ===

    /**
     * 连接到 Everything IPC 服务。
     *
     * @param lpInstanceName 要连接的实例名称 (例如 "1.5a")。使用 null 连接到默认实例。
     * @return 客户端句柄，失败时返回 null。
     */
    EverythingClient Everything3_ConnectW(WString lpInstanceName);

    /**
     * 从 Everything IPC 服务断开连接并释放客户端资源。
     *
     * @param client 要销毁的客户端句柄。
     */
    void Everything3_DestroyClient(EverythingClient client);

    /**
     * 获取Everything是否已完成构建引索的状况
     *
     * @param client 客户端句柄
     * @return 是否完成构建引索
     */
    boolean Everything3_IsDBLoaded(EverythingClient client);

    // === 2. 搜索配置 ===

    /**
     * 创建一个新的、空的搜索状态对象。
     *
     * @return 搜索状态句柄，失败时返回 null。
     */
    EverythingSearchState Everything3_CreateSearchState();

    /**
     * 销毁一个搜索状态对象并释放其资源。
     *
     * @param search_state 要销毁的搜索状态句柄。
     */
    void Everything3_DestroySearchState(EverythingSearchState search_state);

    /**
     * 为搜索状态设置搜索文本。
     *
     * @param search_state 搜索状态句柄。
     * @param lpSearchText 搜索查询字符串。
     */
    void Everything3_SetSearchTextW(EverythingSearchState search_state, WString lpSearchText);

    /**
     * 设置视口中要返回的最大结果数。
     *
     * @param search_state 搜索状态句柄。
     * @param count        要请求的结果数量。
     */
    void Everything3_SetSearchViewportCount(EverythingSearchState search_state, WinDef.DWORD count);

    /**
     * 添加搜索时要获取的属性 ,属性见{@link PropertyType}
     *
     * @param searchState 搜索状态句柄。
     * @param propertyID  属性类型
     */
    void Everything3_AddSearchPropertyRequest(EverythingSearchState searchState, WinDef.DWORD propertyID);

    // === 3. 执行搜索与结果处理 ===

    /**
     * 获取搜索结果中的文件大小
     *
     * @param result_list  结果列表句柄
     * @param result_index 结果的从零开始的索引。
     * @return 文件大小（单位: Byte）
     */
    long Everything3_GetResultSize(EverythingResultList result_list, WinDef.DWORD result_index);

    /**
     * 获取指定索引处结果的完整名称。
     *
     * @param result_list             结果列表句柄。
     * @param result_index            结果的从零开始的索引。
     * @param filename                用于接收文件名的字符缓冲区。
     * @param filename_size_in_wchars 缓冲区的宽字符大小 (在 Java 中是 char)。
     */
    void Everything3_GetResultFilelistFilenameW(EverythingResultList result_list, WinDef.DWORD result_index, char[] filename, WinDef.DWORD filename_size_in_wchars);

    /**
     * 根据属性值获取结果的特定属性
     *
     * @param resultList 结果列表句柄。
     * @param result_index 结果的从零开始的索引。
     * @param propertyID 需要获取的属性的属性值
     * @param buffer 用于接收内容的字符缓冲区。
     * @param filename_size_in_wchars 缓冲区的宽字符大小 (在 Java 中是 char)。
     */
    void Everything3_GetResultPropertyTextW(EverythingResultList resultList, WinDef.DWORD result_index, WinDef.DWORD propertyID, char[] buffer, WinDef.DWORD filename_size_in_wchars);


    /**
     * 使用给定的客户端和搜索状态执行搜索。
     *
     * @param client       已连接的客户端句柄。
     * @param search_state 已配置的搜索状态句柄。
     * @return 结果列表句柄，失败时返回 null。
     */
    EverythingResultList Everything3_Search(EverythingClient client, EverythingSearchState search_state);

    /**
     * 销毁一个结果列表对象并释放其资源。
     *
     * @param result_list 要销毁的结果列表句柄。
     */
    void Everything3_DestroyResultList(EverythingResultList result_list);

    /**
     * 获取当前结果列表视口中的结果数量。
     *
     * @param result_list 结果列表句柄。
     * @return 结果的数量。
     */
    WinDef.DWORD Everything3_GetResultListViewportCount(EverythingResultList result_list);

    /**
     * 获取指定索引处结果的完整路径和名称。
     *
     * @param result_list             结果列表句柄。
     * @param result_index            结果的从零开始的索引。
     * @param filename                用于接收文件名的字符缓冲区。
     * @param filename_size_in_wchars 缓冲区的宽字符大小 (在 Java 中是 char)。
     */
    void Everything3_GetResultFullPathNameW(EverythingResultList result_list, WinDef.DWORD result_index, char[] filename, WinDef.DWORD filename_size_in_wchars);


    // --- 使用示例 ---
    public static void main(String[] args) {
        EverythingClient client = null;
        EverythingSearchState searchState = null;
        EverythingResultList resultList = null;

        try {
            // --- 步骤 1: 连接到 Everything 服务 ---
            System.out.println("正在尝试连接到 'vortex_backend' 的 Everything 实例...");
            client = INSTANCE.Everything3_ConnectW(new WString("vortex_backend"));

            if (client == null) {
                System.err.println("无法连接到任何 'vortex_backend' Everything 实例。请确保 Everything 1.5 正在运行。");
                return;
            }
            System.out.println("成功连接到 Everything。");

            // --- 步骤 2: 创建和配置搜索条件 ---
            searchState = INSTANCE.Everything3_CreateSearchState();
            if (searchState == null) {
                System.err.println("创建搜索状态失败。");
                return;
            }

            // 设置搜索关键字
            String query = "win.ini";
            INSTANCE.Everything3_SetSearchTextW(searchState, new WString(query));

            // (可选) 设置返回结果的最大数量
            INSTANCE.Everything3_SetSearchViewportCount(searchState, new WinDef.DWORD(100));

            // --- 步骤 3: 执行搜索 ---
            System.out.println("正在执行搜索 '" + query + "'...");
            resultList = INSTANCE.Everything3_Search(client, searchState);
            if (resultList == null) {
                System.err.println("搜索执行失败。");
                return;
            }

            // --- 步骤 4: 遍历和处理结果 ---
            WinDef.DWORD numResults = INSTANCE.Everything3_GetResultListViewportCount(resultList);
            System.out.println("找到 " + numResults.intValue() + " 个结果:");

            char[] filenameBuffer = new char[MAX_PATH];
            for (int i = 0; i < numResults.intValue(); i++) {
                INSTANCE.Everything3_GetResultFullPathNameW(resultList, new WinDef.DWORD(i), filenameBuffer, new WinDef.DWORD(MAX_PATH));
                String filename = Native.toString(filenameBuffer);
                System.out.println("  " + filename);
            }

        } finally {
            // --- 步骤 5: 清理资源 (非常重要!) ---
            // 无论是否发生异常，都必须确保释放所有句柄，以避免内存泄漏。
            System.out.println("\n正在清理资源...");
            if (resultList != null) {
                INSTANCE.Everything3_DestroyResultList(resultList);
            }
            if (searchState != null) {
                INSTANCE.Everything3_DestroySearchState(searchState);
            }
            if (client != null) {
                INSTANCE.Everything3_DestroyClient(client);
            }
            System.out.println("清理完成。");
        }
    }
}
