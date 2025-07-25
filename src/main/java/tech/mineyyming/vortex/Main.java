package tech.mineyyming.vortex;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
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
import org.slf4j.bridge.SLF4JBridgeHandler;
import tech.mineyyming.vortex.model.AppConfig;
import tech.mineyyming.vortex.model.AppConfigManager;
import tech.mineyyming.vortex.service.AutoOperateManager;
import tech.mineyyming.vortex.service.WindowAnimator;
import tech.mineyyming.vortex.ui.MainWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.LogManager;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    AppConfig config = AppConfigManager.getInstance();
    static boolean isAutoStart = false;
    static FXTrayIcon icon;

    public static void main(String[] args) {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        logger.info("Starting vortex");

        // 检查命令行参数
        for (String arg : args) {
            if ("--autostart".equalsIgnoreCase(arg)) {
                isAutoStart = true;
                logger.info("程序为开机自启动");
                break;
            }
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Platform.setImplicitExit(false);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/mineyyming/vortex/ui/main-window.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        //String cssPath = Objects.requireNonNull(getClass().getResource("fluent-style.css")).toExternalForm();
        //scene.getStylesheets().add(cssPath);

        // Set the scene to the stage
        primaryStage.setScene(scene);

        MainWindow controller = loader.getController();
        logger.info("FXML加载成功");

        controller.setStage(primaryStage);
        controller.setPrimaryStage();
        controller.setupGlobalKeyListener();
        controller.setOtherListeners();

        AutoOperateManager.setAutoFocus(primaryStage,"searchField");
        setupTrayMenu(primaryStage);

        if(!isAutoStart) {
            WindowAnimator.showWindow(primaryStage);
            logger.info("用户界面显示成功");
        }else {
            //icon.showMessage("Vortex","程序已启动");
            logger.info("程序加载成功");
        }
    }

    /**
     * 新增的部分：当应用关闭时，这个方法会被调用
     */
    @Override
    public void stop() throws Exception {
        AppConfigManager.save();
        try {
            // 注销全局钩子，释放资源
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }
        icon.hide();
        logger.info("JNativeHook 已注销，FXTrayIcon 已注销，程序退出。");
        super.stop();
    }

    public void setupTrayMenu(Stage primaryStage) {
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
//            if(primaryStage.isAlwaysOnTop()){
//                config.setAutoCloseOnFocusLoss(false);
//                logger.info("取消失焦关闭");
//            } else {
//                config.setAutoCloseOnFocusLoss(true);
//                logger.info("开启失焦关闭");
//            }
            config.setAutoCloseOnFocusLoss(!config.getAutoCloseOnFocusLoss());
        });

        MenuItem openItem = new MenuItem();
        openItem.setOnAction(event -> {
            if(primaryStage.isShowing()) {
                WindowAnimator.hideWindow(primaryStage);
            } else {
                WindowAnimator.showWindow(primaryStage);
            }
        });

        openItem.textProperty().bind(Bindings.when(primaryStage.showingProperty()).then("close window").otherwise("open window"));
        MenuItem exitItem = new MenuItem("exit");
        exitItem.setOnAction(event -> {
            if(primaryStage.isShowing()){
                WindowAnimator.hideWindow(primaryStage,Platform::exit);
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
