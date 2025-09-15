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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.minediamond.vortex.model.appConfig.GlobalDataStore;
import tech.minediamond.vortex.model.program.ProgramInfo;
import tech.minediamond.vortex.model.program.ProgramSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class findApplications {
    public static void getInstalledPrograms() throws IOException, InterruptedException {
        // PowerShell 命令
        String command = """
                chcp 65001 | Out-Null
                [Console]::OutputEncoding = [System.Text.Encoding]::UTF8;
                $OutputEncoding = [System.Text.Encoding]::UTF8

                & {
                    (Get-ItemProperty HKLM:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*, HKLM:\\Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*, HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\* -ErrorAction SilentlyContinue).Where({
                                    $_.DisplayName -and -not $_.SystemComponent
                                })|
                        Select-Object @{Name="baseName"; Expression={$_.DisplayName}},
                                      @{Name="programName"; Expression={
                                                  if ($_.DisplayIcon) {
                                                      $cleanedPath = ($_.DisplayIcon -split ',')[0].Trim('"')
                                                      $fileName = Split-Path -Path $cleanedPath -Leaf
                                                      if ($fileName -like '*.exe') {
                                                          $fileName
                                                      } else {
                                                          $null
                                                      }
                                                  } else {
                                                     $null
                                                  }
                                      }},
                                      @{Name="version"; Expression={$_.DisplayVersion}},
                                      @{Name="publisher"; Expression={$_.Publisher}},
                                      @{Name="installLocation"; Expression={$_.InstallLocation}},
                                      @{Name="source"; Expression={"REGISTRY"}}
                                      
                    (Get-AppxPackage -PackageTypeFilter Main).Where({
                                $_.Name -notlike 'ms-resource:*' -and $_.DisplayName -notlike 'ms-resource:*'
                            })|
                    Select-Object @{Name="baseName"; Expression={$manifest = $_ | Get-AppxPackageManifest
                            $cleanName = $manifest.Package.Properties.DisplayName
                             if ($cleanName -and $cleanName -notlike 'ms-resource:*') {
                                 $cleanName
                             } else {
                                 $_.Name
                             }
                         }},
                      @{Name="programName"; Expression={"$($_.PackageFamilyName)!App"}},
                      @{Name="publisher"; Expression={$_.Publisher}},
                      @{Name="installLocation"; Expression={$_.InstallLocation}},
                      @{Name="source"; Expression={"UWP"}}
                
                } | ConvertTo-Json -Compress
                
        """;

        LocalTime time = LocalTime.now();
        ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-"
        );

        // 将错误流重定向到标准输出流
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 通过 stdin 将脚本写入 PowerShell 进程
        try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(command);
        }

        // 现在只需要读取一个流
        String jsonOutput;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            jsonOutput = reader.lines().collect(Collectors.joining());
        }

        // 等待进程结束并检查退出码
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("PowerShell 命令执行失败，退出码: " + exitCode);
        }

        System.out.println("jsonOutput: " + jsonOutput);

        LocalTime time2 = LocalTime.now();
        System.out.println("powershell执行时间:" + Duration.between(time, time2).toMillis() + "ms");

        ObjectMapper mapper = new ObjectMapper();
        List<ProgramInfo> programs = mapper.readValue(jsonOutput,new TypeReference<List<ProgramInfo>>(){});


        programs.sort(Comparator.comparing(ProgramInfo::getBaseName, Comparator.nullsLast(String::compareToIgnoreCase)));
        programs.removeIf((programInfo) ->{
            return (programInfo.getSource() == ProgramSource.REGISTRY && (programInfo.getProgramName() == null || programInfo.getInstallLocation() == null || programInfo.getInstallLocation().equals("")));
        });
        for (ProgramInfo programInfo : programs) {

            programInfo.setDisplayName(programInfo.getBaseName());
            if(programInfo.getInstallLocation() != null && programInfo.getInstallLocation().equals("")){
                programInfo.setInstallLocation(null);
            }

            if(programInfo.getSource() == ProgramSource.REGISTRY && !programInfo.getInstallLocation().endsWith("\\")){
                programInfo.setInstallLocation(programInfo.getInstallLocation() + "\\");
            }
        }

        GlobalDataStore.programInfos.addAll(programs);

        LocalTime time3 = LocalTime.now();
        System.out.println("数据处理时间:" + Duration.between(time2, time3).toMillis() + "ms");


    }

    public static void main(String[] args) throws IOException, InterruptedException, SQLException {
        LocalTime start = LocalTime.now();
        getInstalledPrograms();
        GlobalDataStore.programInfos.forEach(System.out::println);
        dataBaseOperate db = dataBaseOperate.getInstance();
        db.syncSoftwareList(GlobalDataStore.programInfos);
        LocalTime end = LocalTime.now();
        System.out.println("程序运行总时间: " + Duration.between(start, end).toMillis() + "ms");
    }
}
