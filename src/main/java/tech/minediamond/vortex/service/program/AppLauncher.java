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

package tech.minediamond.vortex.service.program;

import tech.minediamond.vortex.model.appConfig.GlobalDataStore;
import tech.minediamond.vortex.program.ProgramInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public class AppLauncher {

    private static final String UWP_COMMAND_PREFIX = "shell:AppsFolder\\";
    private static final String EXPLORER_EXECUTABLE = "explorer.exe";

    public static ProgramInfo getProgramInfoByID(String id) throws IOException {
        ProgramInfo programInfo = new ProgramInfo();
        boolean found = false;
        for(ProgramInfo info : GlobalDataStore.programInfos) {
            if(info.getId().equals(id)) {
                programInfo = info;
                found = true;
            }
        }
        if(found) {
            return programInfo;
        }else {
            return null;
        }
    }

    public static boolean openApplication(ProgramInfo programInfo) {

        ProcessBuilder pb = new ProcessBuilder();

        switch (programInfo.getSource()) {
            case UWP -> {
                System.out.println("启动UWP应用");

                String aumid = programInfo.getProgramName();
                pb = new ProcessBuilder(EXPLORER_EXECUTABLE, UWP_COMMAND_PREFIX + aumid);

            }
            case REGISTRY -> {
                System.out.println("启动Registry应用");

                Path path = Path.of(programInfo.getInstallLocation(),programInfo.getProgramName());
                System.out.println(path);
                pb = new ProcessBuilder(path.toString());

                File workingDirectory = new File(programInfo.getInstallLocation());
                pb.directory(workingDirectory);
            }
            default -> throw new IllegalArgumentException("不支持的 ProgramSource 类型: " + programInfo.getSource());
        }

        try {
            pb.start();
            return true;
        } catch (IOException e) {
            return false;
        }

    }

    public static void main(String[] args) throws IOException, SQLException {


        dataBaseOperate dbo = dataBaseOperate.getInstance();
        GlobalDataStore.programInfos = dbo.readProgramInfoList();
        GlobalDataStore.programInfos.forEach(System.out::println);
        System.out.println("---------------------------------------------------------------");

        System.out.println(getProgramInfoByID("146"));
        openApplication(getProgramInfoByID("146"));
    }
}
