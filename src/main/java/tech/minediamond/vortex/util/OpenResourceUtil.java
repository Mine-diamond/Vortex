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
import tech.minediamond.vortex.model.search.EverythingResult;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * 对{@code Desktop}的包装，目的是调用方不需要自己验证是否支持桌面操作
 */
@Slf4j
public class OpenResourceUtil {
    private static final Desktop desktop = Desktop.getDesktop();;

    public static boolean OpenFile(EverythingResult result) {
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            try {
                desktop.open(new File(result.getFullPath()));
                return true;
            } catch (IOException e) {
                log.error("打开文件出错, 路径: {}",result.getFullPath(),e);
            } catch (IllegalArgumentException e) {
                log.error("文件不存在, 路径: {}",result.getFullPath(),e);
            }
        }
        return false;
    }

    public static boolean OpenFileInFolder(EverythingResult result) {

        ProcessBuilder pb = new ProcessBuilder("explorer","/select,"+"\""+result.getFullPath()+"\"");
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        try {
            pb.start();
            return true;
        } catch (IOException e) {
            log.error("打开文件 {} 出现错误，文件路径：{}",result.getFileName(),result.getFullPath(),e);
        }
        return false;
    }
}
