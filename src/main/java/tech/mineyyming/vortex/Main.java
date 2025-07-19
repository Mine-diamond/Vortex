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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.bridge.SLF4JBridgeHandler;
import tech.mineyyming.vortex.ui.MainWindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.LogManager;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    static boolean isAutoStart = false;
    static FXTrayIcon icon;

    public static void main(String[] args) {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
        logger.info("Starting vortex");

        // 1. 检查命令行参数
        for (String arg : args) {
            if ("--autostart".equalsIgnoreCase(arg)) {
                isAutoStart = true;
                logger.info("程序为开机自启动");
                break; // 找到后即可退出循环
            }
        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Platform.setImplicitExit(false);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/mineyyming/vortex/ui/main-window.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));

        MainWindow controller = loader.getController();
        logger.info("FXML加载成功");

        controller.setStage(primaryStage);
        controller.setPrimaryStage();
        controller.setupGlobalKeyListener();
        controller.setOtherListeners();

        setupTrayMenu(primaryStage);

        if(!isAutoStart) {
            primaryStage.show();
            primaryStage.centerOnScreen();
            logger.info("用户界面显示成功");
        }else {
            icon.showInfoMessage("Vortex","程序已启动");
            logger.info("程序加载成功");
        }
    }

    /**
     * 新增的部分：当应用关闭时，这个方法会被调用
     */
    @Override
    public void stop() throws Exception {
        try {
            // 注销全局钩子，释放资源
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }
        System.out.println("JNativeHook 已注销，程序退出。");
        super.stop();
    }

    public void setupTrayMenu(Stage primaryStage) {
        icon = new FXTrayIcon(primaryStage, getClass().getResource("/images/app_icon.png"));
        icon.setTooltip("Vortex 快捷面板");
//        icon.setOnAction(event -> {
//            if(primaryStage.isShowing()) {
//                primaryStage.hide();
//            } else {
//                primaryStage.show();
//            }
//        });
        MenuItem pinItem = new MenuItem();
        pinItem.textProperty().bind(Bindings.when(primaryStage.alwaysOnTopProperty()).then("window on top:true").otherwise("window on top:false"));
        pinItem.setOnAction(event -> {
            if(primaryStage.isAlwaysOnTop()){
                primaryStage.setAlwaysOnTop(false);
                System.out.println("取消置顶");
            } else {
                primaryStage.setAlwaysOnTop(true);
                System.out.println("置顶");
            }
        });

        MenuItem openItem = new MenuItem();
        openItem.setOnAction(event -> {
            if(primaryStage.isShowing()) {
                primaryStage.hide();
            } else {
                primaryStage.show();
            }
        });

        openItem.textProperty().bind(Bindings.when(primaryStage.showingProperty()).then("close window").otherwise("open window"));
        MenuItem exitItem = new MenuItem("exit");
        exitItem.setOnAction(event -> {Platform.exit();System.exit(0);});


        icon.addMenuItem(pinItem);
        icon.addMenuItem(new MenuItem("-"));
        icon.addMenuItem(openItem);
        icon.addMenuItem(exitItem);
        icon.show();
    }
}
