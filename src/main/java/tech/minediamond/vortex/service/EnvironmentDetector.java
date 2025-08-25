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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class EnvironmentDetector {

    private static final Environment environment = detectMode();

    public enum Environment {
        DEVELOPMENT, PRODUCTION
    }

    private static Environment detectMode(){
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        Environment environment;
        if(runtimeMXBean.getInputArguments().contains("-DAPP_ENV=dev")){
            environment = Environment.DEVELOPMENT;
        } else {
            environment = Environment.PRODUCTION;
        }

        return environment;
    }

    public static Environment getAppMode() {
        return environment;
    }

    public static boolean isProduction() {
        return environment.equals(Environment.PRODUCTION);
    }
}
