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

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.bridge.SLF4JBridgeHandler;
import tech.minediamond.vortex.config.AppModule;
import tech.minediamond.vortex.service.*;

import java.awt.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.LogManager;

/**
 * 这是程序真正的主类，负责启动整个程序
 */
@Slf4j
public class Main extends Application {

    private static final int SINGLE_INSTANCE_PORT = 38727;// 端口号
    private static final String FOCUS_COMMAND = "VORTEX::FOCUS_WINDOW";

    private Injector injector;
    private TrayMenuService trayMenuService;

    private boolean resourceLoaded = false;
    private ServerSocket singleInstanceSocket;

    public static void main(String[] args) {

        log.info("""
                
                ------------------------------------------------------------------------
                Starting vortex
                ------------------------------------------------------------------------
                """);


        //初始化日志系统
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();

        //输出当前的环境参数
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = runtimeMxBean.getInputArguments();
        log.info("JVM 参数: {}", jvmArgs);
        log.info("系统版本: {}", System.getProperty("os.name"));
        log.info("执行环境：{}",runtimeMxBean.getInputArguments().contains("-DAPP_ENV=prod") ? "发行版运行" : "源码运行");
        log.info("运行路径: {}",runtimeMxBean.getInputArguments().contains("-DAPP_ENV=prod") ? System.getProperty("jpackage.app-path"):System.getProperty("user.dir"));

        //执行start方法
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //检查运行环境和单例
        if (!checkEnvironment()) {return;}
        if (!singleInstanceSocket()) {return;}

        resourceLoaded = true;

        //在所有检查之后加载服务和错误处理器
        this.injector = Guice.createInjector(new AppModule());
        Thread.setDefaultUncaughtExceptionHandler(injector.getInstance(GlobalUncaughtExceptionHandlerService.class));

        //初始化服务
        StageProvider stageProvider = injector.getInstance(StageProvider.class);
        stageProvider.setStage(primaryStage);
        trayMenuService = injector.getInstance(TrayMenuService.class);//应确保在stageProvider.setStage()之后调用


        //初始化界面
        Platform.setImplicitExit(false);//所有窗口关闭后程序不会关闭
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/minediamond/vortex/ui/main-window.fxml"));
        loader.setControllerFactory(injector::getInstance);
        loader.setResources(injector.getInstance(I18nService.class).getResourceBundle());
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        ThemeService themeService = injector.getInstance(ThemeService.class);
        themeService.registerScene(scene);

        primaryStage.setScene(scene);

        boolean isAutoStart = getParameters().getUnnamed().contains("--autostart");
        WindowAnimator windowAnimator = injector.getInstance(WindowAnimator.class);
        if (!isAutoStart) {
            windowAnimator.showWindow(primaryStage, true);
            log.info("用户界面显示成功");
        } else {
            log.info("程序加载成功，进入后台运行");
        }
    }

    /**
     * 当应用关闭时，这个方法会被调用
     * <p>
     * 当[JavaFX Application Thread]出现未捕获的错误时该方法也会执行
     */
    @Override
    public void stop() throws Exception {

        if (singleInstanceSocket != null && !singleInstanceSocket.isClosed()) {
            singleInstanceSocket.close();
            log.info("已关闭端口监听");
        }

        if (resourceLoaded) {
            log.info("vortex 即将退出，正在保存和清理资源...");

            runSafely("保存配置文件",()-> {injector.getInstance(AppConfigService.class).save();});
            runSafely("注销JNativeHook",()-> {GlobalScreen.unregisterNativeHook();});
            runSafely("注销FXTrayIcon",()->{trayMenuService.closeTrayMenu();});
            runSafely("退出Everything",()->{injector.getInstance(EverythingService.class).stopEverythingInstance();});

        }

        log.info("程序退出。");

        log.info("""
                
                ------------------------------------------------------------------------
                vortex stopped
                ------------------------------------------------------------------------
                """);

        super.stop();
    }

    //其实这个类的作用是stop()方法中不需要再写特别多的try...catch，更漂亮和清晰一些
    private void runSafely(String action,CheckedRunnable r) {
        try {
            r.run();
            log.info("{}成功", action);
        } catch (Throwable t) {
            log.error("{}失败: {}", action, t.getMessage(), t);
        }
    }

    //避免在Runnable中写try...catch
    @FunctionalInterface
    interface CheckedRunnable {
        void run() throws Exception;
    }

    private boolean checkEnvironment() {
        try {
            //检查操作系统
            if (!checkSystem()) {
                return false;
            }

            //检查无头环境
            if (!checkHeadlessEnvironment()) {
                return false;
            }

            //检查everything文件
            if (!checkEverythingFileExist()) {
                return false;
            }
        } catch (Exception e) {
            log.error("在检查环境时出错: {}", e.getMessage(), e);
            Platform.exit();
        }
        return true;
    }

    //以下的check方法均为返回true为pass,false为not pass
    private boolean checkSystem() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            systemAlertStop();
            log.error("系统不支持");
            Platform.exit();
            return false;
        }
        return true;
    }

    private boolean checkHeadlessEnvironment() {
        if (GraphicsEnvironment.isHeadless()) {
            log.error("环境为无头环境");
            System.err.println("Please run in Environment that support UI environment");
            Platform.exit();
            return false;
        }
        return true;
    }

    private boolean checkEverythingFileExist() {
        final String EVERYTHING_PATH = Paths.get("everything\\Everything64.exe").toFile().getAbsolutePath();
        File file = new File(EVERYTHING_PATH);
        if (!file.exists()) {
            everythingAlertStop();
            log.error("引索程序未找到");
            Platform.exit();
            return false;
        }
        return true;
    }

    private void systemAlertStop() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("error");
        alert.setHeaderText("System not supported");
        alert.setContentText("Vortex only supports running on Windows systems\nIt does not support running on Linux, Mac, or other\nsystems");
        alert.showAndWait();
    }

    private void everythingAlertStop(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("error");
        alert.setHeaderText("File indexing service not found");
        alert.setContentText("Try redownload and reinstall Vortex to resolve the issue.");
        alert.showAndWait();
    }

    private boolean singleInstanceSocket() {
        try {
            singleInstanceSocket = new ServerSocket(SINGLE_INSTANCE_PORT, 10, InetAddress.getLoopbackAddress());
            startInstanceListener();
            log.info("已连接端口");
            return true;
        } catch (IOException e) {
            log.warn("端口已被占用");
            sendFocusCommandToFirstInstance();
            Platform.exit();
            return false;
        }
    }

    private void startInstanceListener() {
        Thread listenerThread = new Thread(() -> {
            while (!singleInstanceSocket.isClosed()) {
                try (Socket clientSocket = singleInstanceSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    String command = in.readLine();
                    if (FOCUS_COMMAND.equals(command)) {
                        // 收到唤醒命令，需要在 JavaFX 应用线程中操作 UI
                        Platform.runLater(() -> {
                            log.info("已接收命令，将窗口带入前台。");
                            if(injector!=null){
                                injector.getInstance(WindowAnimator.class).showMainWindow();
                            }
                        });
                    }
                } catch (IOException e) {
                    // ServerSocket 关闭时会抛出异常，这是正常的退出方式
                    if (singleInstanceSocket.isClosed()) {
                        log.info("实例监听器线程关闭");
                        break;
                    }
                    log.error(e.getMessage(), e);
                }
            }
        });

        // 设置为守护线程，这样它不会阻止 JVM 退出
        listenerThread.setDaemon(true);
        listenerThread.setName("Vortex Instance Listener");
        listenerThread.start();
    }

    private void sendFocusCommandToFirstInstance() {
        try (Socket clientSocket = new Socket(InetAddress.getLocalHost(), SINGLE_INSTANCE_PORT);
             OutputStream out = clientSocket.getOutputStream()) {

            out.write((FOCUS_COMMAND + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            log.info("聚焦指令发送成功。即将退出。");

        } catch (IOException e) {
            log.error("发送聚焦指令失败");
            log.error(e.getMessage(), e);
        }
    }

}
