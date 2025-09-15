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

import lombok.Getter;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * 用于检测程序运行环境的工具类
 */
public class EnvironmentDetector {

    @Getter
    private static final Environment environment = detectMode();

    public enum Environment {
        DEVELOPMENT, PRODUCTION
    }

    /**
     * 检测运行环境的核心方法
     * @return 目前的运行环境
     */
    private static Environment detectMode(){
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        Environment environment;
        if(runtimeMXBean.getInputArguments().contains("-DAPP_ENV=prod")){
            environment = Environment.PRODUCTION;
        } else {
            environment = Environment.DEVELOPMENT;
        }

        return environment;
    }

    /**
     *
     * @return 返回目前是否是生产环境
     */
    public static boolean isProduction() {
        return environment.equals(Environment.PRODUCTION);
    }
}
