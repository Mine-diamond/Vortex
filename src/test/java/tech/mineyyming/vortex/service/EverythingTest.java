package tech.mineyyming.vortex.service;

import tech.mineyyming.vortex.model.*;
import tech.mineyyming.vortex.service.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;

public class EverythingTest {
    public static void main(String[] args) {
        try {
            EverythingManager manager = new EverythingManager();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            System.out.println("--- 示例 1: 在 Program Files 目录中搜索所有 .exe 文件 ---");
            Path progFiles = Paths.get(System.getenv("ProgramFiles"));
            List<EverythingResult> exeResults = manager
                    .searchFor("*.exe")
                    .inFolders(progFiles)
                    .mode(SearchMode.FILES_ONLY)
                    .request(RequestFlag.SIZE, RequestFlag.DATE_MODIFIED)
                    .query();

            System.out.printf("在 '%s' 中找到 %d 个 .exe 文件。%n", progFiles, exeResults.size());
            exeResults.stream().limit(5).forEach(r ->
                    System.out.printf("  - [%s] %s (%.2f MB, 修改于 %s)%n",
                            r.getType(), r.getFileName(), r.getSize() / (1024.0 * 1024.0), sdf.format(r.getDateModified()))
            );

            System.out.println("\n--- 示例 2: 在全局搜索名为 'docs' 的文件夹 ---");
            List<EverythingResult> folderResults = manager
                    .searchFor("docs")
                    .inFolders() // 重置为全局搜索
                    .mode(SearchMode.FOLDERS_ONLY)
                    .query();

            System.out.printf("在全局找到 %d 个名为 'docs' 的文件夹。%n", folderResults.size());
            folderResults.stream().limit(5).forEach(r ->
                    System.out.printf("  - [%s] %s%n", r.getType(), r.getFullPath())
            );

        } catch (UnsatisfiedLinkError e) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("致命错误: JNA 无法加载 Everything64.dll。");
            System.err.println("请确认以下几点:");
            System.err.println("1. 你已经安装并正在运行 Everything 软件。");
            System.err.println("2. `Everything64.dll` 文件存在于项目的 `src/main/resources/win32-x86-64/` 目录下。");
            System.err.println("3. 你的 Java-JVM 是 64 位的。");
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
