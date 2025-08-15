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

package tech.minediamond.vortex.testForNew;

import mslinks.ShellLink;
import mslinks.ShellLinkException;

import java.io.File;
import java.io.IOException;

public class LnkParserExample {
    public static void main(String[] args) {
        // 指定要解析的.lnk文件路径
        String lnkFilePath = "C:\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs\\Cherry Studio.lnk";

        try {
            // 创建ShellLink对象来解析.lnk文件
            ShellLink shellLink = new ShellLink(new File(lnkFilePath));

            // 获取快捷方式的各种属性
            System.out.println("=== 快捷方式信息 ===");
            System.out.println("目标路径: " + shellLink.getRelativePath());
            System.out.println("工作目录: " + shellLink.getWorkingDir());
            System.out.println("命令行参数: " + shellLink.getCMDArgs());
            System.out.println("图标位置: " + shellLink.getIconLocation());
            System.out.println(shellLink.getLinkInfo());
            System.out.println(shellLink.getConsoleData().getFontSize());
            System.out.println(shellLink.getHeader().getIconIndex());
            System.out.println(shellLink.getName());


        } catch (IOException e) {
            System.err.println("读取文件时出错: " + e.getMessage());
            e.printStackTrace();
        } catch (ShellLinkException e) {
            System.err.println("解析快捷方式时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

