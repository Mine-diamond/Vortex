package tech.minediamond.vortex;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.bridge.SLF4JBridgeHandler;
import tech.minediamond.vortex.config.AppModule;
import tech.minediamond.vortex.model.AppConfig;
import tech.minediamond.vortex.service.ConfigService;
import tech.minediamond.vortex.service.ThemeService;
import tech.minediamond.vortex.service.WindowAnimator;
import tech.minediamond.vortex.ui.MainWindow;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.logging.LogManager;

/**
 * 程序的主类，负责启动整个程序
 */
@Slf4j
public class Main extends Application {
    private Injector injector;
    static boolean isAutoStart = false;
    static FXTrayIcon icon;
    WindowAnimator windowAnimator;

    public static void main(String[] args) {
        //初始化日志系统
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        log.info("Starting vortex");

        //输出当前的JVM参数
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = runtimeMxBean.getInputArguments();
        log.info("JVM arguments: {}", jvmArgs);

        // 检查命令行参数
        for (String arg : args) {
            if ("--autostart".equalsIgnoreCase(arg)) {
                isAutoStart = true;
                log.info("程序为开机自启动");
                break;
            }
        }

        //执行start方法
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
        this.injector = Guice.createInjector(new AppModule());

        windowAnimator = injector.getInstance(WindowAnimator.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Platform.setImplicitExit(false);//所有窗口关闭后程序不会关闭
        setupShutDownHook();
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/minediamond/vortex/ui/main-window.fxml"));
        loader.setControllerFactory(injector::getInstance);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        ThemeService themeManager = injector.getInstance(ThemeService.class);
        themeManager.initialize(scene);

        primaryStage.setScene(scene);

        MainWindow controller = loader.getController();
        log.info("FXML加载成功");

        controller.setStage(primaryStage);
        controller.setupStageProperties();
        controller.setupGlobalKeyListener();
        controller.setupWindowListeners();

        setupTrayMenu(primaryStage);

        if (!isAutoStart) {
            windowAnimator.showWindow(primaryStage);
            log.info("用户界面显示成功");
        } else {
            log.info("程序加载成功");
        }
    }

    /**
     * 当应用关闭时，这个方法会被调用
     */
    @Override
    public void stop() throws Exception {
        ConfigService configService = injector.getInstance(ConfigService.class);
        configService.save();
        try {
            // 注销全局钩子，释放资源
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException ex) {
            log.error("注销全局钩子出错, {}", ex.getMessage());
        }
        icon.hide();
        log.info("JNativeHook 已注销，FXTrayIcon 已注销，程序退出。");
        super.stop();
    }

    /**
    * 创建并注册一个 Shutdown Hook，当程序正常或异常退出时总会执行
    */
    public void setupShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🔌 Shutdown Hook 正在执行...还没有任何逻辑喵~");
        }));
    }

    public void setupTrayMenu(Stage primaryStage) {
        AppConfig config = injector.getInstance(AppConfig.class);
        icon = new FXTrayIcon(primaryStage, getClass().getResource("/images/app_icon_x16.png"));
        icon.setTooltip("Vortex 快捷面板");
//        icon.setOnAction(event -> {
//            if(primaryStage.isShowing()) {
//                primaryStage.hide();
//            } else {
//                primaryStage.show();
//            }
//        });
        MenuItem pinItem = new MenuItem();
        pinItem.textProperty().bind(Bindings.when(config.autoCloseOnFocusLossProperty()).then("Close when loses focus").otherwise("Don't Close when loses focus"));
        pinItem.setOnAction(event -> {
            config.setAutoCloseOnFocusLoss(!config.getAutoCloseOnFocusLoss());
        });

        MenuItem openItem = new MenuItem();
        openItem.setOnAction(event -> {
            if (primaryStage.isShowing()) {
                windowAnimator.hideWindow(primaryStage);
            } else {
                windowAnimator.showWindow(primaryStage);
            }
        });

        openItem.textProperty().bind(Bindings.when(primaryStage.showingProperty()).then("close window").otherwise("open window"));
        MenuItem exitItem = new MenuItem("exit");
        exitItem.setOnAction(event -> {
            if (primaryStage.isShowing()) {
                windowAnimator.hideWindow(primaryStage, Platform::exit);
            } else {
                Platform.exit();
            }
        });

        icon.addMenuItem(pinItem);
        icon.addMenuItem(new MenuItem("-"));
        icon.addMenuItem(openItem);
        icon.addMenuItem(exitItem);
        icon.show();
    }

}
