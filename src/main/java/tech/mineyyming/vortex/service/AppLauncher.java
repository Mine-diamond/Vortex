package tech.mineyyming.vortex.service;

import tech.mineyyming.vortex.model.GlobalDataStore;
import tech.mineyyming.vortex.model.ProgramInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Optional;

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
