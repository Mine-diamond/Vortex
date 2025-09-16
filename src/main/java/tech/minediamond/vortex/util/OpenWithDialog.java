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

package tech.minediamond.vortex.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.win32.W32APIOptions;

import java.io.IOException;

public interface OpenWithDialog extends Library {
    Shell32 INSTANCE = Native.load("shell32", Shell32.class, W32APIOptions.DEFAULT_OPTIONS);

    // --- 常量定义 ---
    // S_OK 表示成功
    int S_OK = 0;

    // OPEN_AS_INFO_FLAGS 枚举值
    // 允许用户选中 "始终使用此应用打开..."
    int OAIF_ALLOW_REGISTRATION = 0x00000001;
    // 如果用户选择了程序，则立即执行打开操作
    int OAIF_EXEC = 0x00000004;

    // 2. 映射 OPENASINFO 结构体
    @Structure.FieldOrder({"pcszFile", "pcszClass", "oaifIn"})
    class OPENASINFO extends Structure {
        public String pcszFile;  // 对应 LPCWSTR pcszFile
        public String pcszClass; // 对应 LPCWSTR pcszClass
        public int oaifIn;       // 对应 OPEN_AS_INFO_FLAGS oaifIn
    }

    // 3. 声明 SHOpenWithDialog 函数
    // C++: HRESULT SHOpenWithDialog(HWND hwndParent, const OPENASINFO *poainfo);
    // HRESULT 在 JNA 中映射为 int
    // HWND 在 JNA 中映射为 int (或 Pointer)
    // const OPENASINFO* 在 JNA 中直接映射为 OPENASINFO 类的实例
    int SHOpenWithDialog(int hwndParent, OPENASINFO poainfo);

    public static void main(String[] args) throws IOException {

    }
}
