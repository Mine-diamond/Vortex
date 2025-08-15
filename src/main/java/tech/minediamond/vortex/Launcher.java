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

package tech.minediamond.vortex;

/**
 * 这是一个包装启动类，用于解决 jpackage 和 fat-jar 的模块化冲突问题。
 * 这个类本身不是 JavaFX 应用，它的 main 方法只是简单地调用真正的 Main 类的 main 方法。
 */
public class Launcher {
    public static void main(String[] args) {
        // 调用真正的 Main 类的 main 方法
        Main.main(args);
    }
}