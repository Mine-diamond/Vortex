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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class EverythingProcessService {

    Process everythingProcess;
    String e = Paths.get("everything\\everything.exe").toFile().getAbsolutePath();

    @Inject
    public EverythingProcessService() {

    }

    public void startEverything() throws IOException, InterruptedException {
        System.out.println(e);
        ProcessBuilder pb = new ProcessBuilder(e, "-instance", "vortex_backend");
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        everythingProcess = pb.start();
    }

    public void endEverything() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(e, "-exit", "-instance", "vortex_backend");
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        everythingProcess = pb.start();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        EverythingProcessService everythingProcessService = new EverythingProcessService();
        everythingProcessService.startEverything();
        System.out.println("run");
        TimeUnit.SECONDS.sleep(30);
        if (everythingProcessService.everythingProcess.isAlive()) {
            System.out.println("alive");
            everythingProcessService.endEverything();

        }
    }
}
