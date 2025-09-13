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
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.bridge.SLF4JBridgeHandler;
import tech.minediamond.vortex.config.AppModule;
import tech.minediamond.vortex.service.*;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.logging.LogManager;

/**
 * 这是程序真正的主类，负责启动整个程序
 */
@Slf4j
public class Main extends Application {

    private static final String AUTO_START = "--autostart";
    private static final String APP_ENV_PROD_FLAG = "-DAPP_ENV=prod";

    private Injector injector;
    private TrayMenuService trayMenuService;

    private boolean resourceLoaded = false;

    public static void main(String[] args) {

        //初始化日志系统
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();

        log.info("""
                
                ------------------------------------------------------------------------
                Starting vortex
                ------------------------------------------------------------------------
                """);

        //输出当前的环境参数
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        boolean isProd = runtimeMxBean.getInputArguments().contains(APP_ENV_PROD_FLAG);
        String runPath = isProd ? System.getProperty("jpackage.app-path") : System.getProperty("user.dir");
        log.info("JVM 参数: {}", runtimeMxBean.getInputArguments());
        log.info("系统版本: {}", System.getProperty("os.name"));
        log.info("执行环境：{}",isProd ? "发行版运行" : "源码运行");
        log.info("运行路径: {}",runPath);

        //执行start方法
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //检查运行环境和单例
        if (!EnvironmentChecker.check()) {return;}
        if (!SingleInstanceSocketManager.startSocket()) {return;}

        resourceLoaded = true;

        //在所有检查之后加载服务和错误处理器
        this.injector = Guice.createInjector(new AppModule());
        Thread.setDefaultUncaughtExceptionHandler(injector.getInstance(GlobalUncaughtExceptionHandlerService.class));

        //初始化服务
        injector.getInstance(StageProvider.class).setStage(primaryStage);
        trayMenuService = injector.getInstance(TrayMenuService.class);//应确保在stageProvider.setStage()之后调用
        SingleInstanceSocketManager.setStageReady(injector.getInstance(WindowAnimator.class));


        //初始化界面
        Platform.setImplicitExit(false);//所有窗口关闭后程序不会关闭
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/minediamond/vortex/ui/main-window.fxml"));
        loader.setControllerFactory(injector::getInstance);
        loader.setResources(injector.getInstance(I18nService.class).getResourceBundle());
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        injector.getInstance(ThemeService.class).registerScene(scene);//注册主题服务

        primaryStage.setScene(scene);

        boolean isAutoStart = getParameters().getUnnamed().contains(AUTO_START);
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

        log.info("vortex 即将退出");

        SingleInstanceSocketManager.closeSocket();

        if (resourceLoaded) {
            log.info("正在保存和清理资源...");

            runSafely("保存配置文件",()-> injector.getInstance(AppConfigService.class).save());
            runSafely("注销JNativeHook", GlobalScreen::unregisterNativeHook);
            runSafely("注销FXTrayIcon",()-> trayMenuService.closeTrayMenu());
            runSafely("退出Everything",()-> injector.getInstance(EverythingService.class).stopEverythingInstance());

        }

        log.info("程序退出。");

        log.info("""
                
                ------------------------------------------------------------------------
                vortex stopped
                ------------------------------------------------------------------------
                """);

        super.stop();
    }

    //这个类的作用是stop()方法中不需要再写特别多的try...catch，更漂亮和清晰一些
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

}
