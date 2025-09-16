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

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

@Slf4j
public class ClipboardUtil {
    /**
     * 将指定的字符串复制到系统剪贴板。
     *
     * @param text 要复制的字符串。
     */
    public static void copyToClipboard(String text) {
        try {
            // 1. 获取系统剪贴板
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            // 2. 创建一个 StringSelection 对象，它封装了要传输的字符串
            StringSelection stringSelection = new StringSelection(text);

            // 3. 将 StringSelection 对象放入剪贴板
            clipboard.setContents(stringSelection, null);

            log.info("成功将内容复制到剪贴板: \"{}\"", text);

        } catch (HeadlessException e) {
            log.error("无法访问剪贴板。", e);
        } catch (Exception e) {
            log.error("复制到剪贴板时发生错误。", e);
        }
    }
}
