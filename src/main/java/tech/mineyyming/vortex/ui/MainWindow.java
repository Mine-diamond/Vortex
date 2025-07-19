package tech.mineyyming.vortex.ui;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;
import tech.mineyyming.vortex.model.ContentPanel;
import tech.mineyyming.vortex.service.ShowStageListener;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainWindow {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MainWindow.class);
    private static ContentPanel currentContentPanel;

    @FXML
    private AnchorPane mainWindow;
    @FXML
    private AnchorPane tabWindow;
    //缓存已经加载的视图
    private Map<String, Parent> viewCache = new HashMap<>();

    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isStageShown = true;
    private boolean isExplanding = false;

    private boolean isHiddenNotFoucs = false;

    Stage stage;

    public void initialize() {

        mainWindow.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.ESCAPE) {
                stage.hide();
            }
        });


//        mainWindow.setOnMouseClicked(event -> {
//            mainWindow.requestFocus();
//        });

        mainWindow.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        mainWindow.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset);
                double y = event.getScreenY() - yOffset < Screen.getPrimary().getVisualBounds().getMaxY() - 50 ? event.getScreenY() - yOffset : Screen.getPrimary().getVisualBounds().getMaxY() - 50;
                stage.setY(y);
        });

        loadOrGetView(ContentPanel.EDITORPANEL);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setPrimaryStage(){
        stage.setTitle("Vortex");
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
    }

    //  这个方法将在 stage 被设置后，由 Main 类手动调用
    public void setupGlobalKeyListener() {
        // 确保 stage 不是 null
        if (this.stage == null) {
            logger.error("错误：在设置 Stage 之前调用了 setupGlobalKeyListener！");
            return;
        }

        logger.info("setupGlobalKeyListener() called. Stage is now available.");

        try {
            // 注册全局钩子
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            logger.error("注册全局钩子时出现问题。",ex);
            System.exit(1);
        }

        // 现在 stage 肯定不是 null，可以安全地创建监听器了
        ShowStageListener showStageListener = new ShowStageListener(this.stage);
        GlobalScreen.addNativeKeyListener(showStageListener);
        logger.info("全局按键监听器已成功设置！");
    }

    public void setOtherListeners() {
        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue && isHiddenNotFoucs) {
                stage.hide();
            }
        });
    }

    private void loadOrGetView(ContentPanel fxmlFileName) {
        if(fxmlFileName == currentContentPanel) {
            logger.info("原页面：{}，新页面：{}，页面一致", fxmlFileName.getFileName(), currentContentPanel.getFileName());
            return;
        }
        String fileName = fxmlFileName.getFileName();
        Parent view = viewCache.get(fileName);

        if (view == null) {
            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fileName)));
                view = loader.load();
                viewCache.put(fileName, view); // 加载后放入缓存

                // 让新视图充满 tabWindow (可选，但推荐)
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);
                logger.info("原页面：{}，新页面：{}，从文件加载新页面",Optional.ofNullable(currentContentPanel).map(ContentPanel::getFileName).orElse("无"), fxmlFileName.getFileName());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            logger.info("原页面：{}，新页面：{}，从缓存加载新页面",Optional.ofNullable(currentContentPanel).map(ContentPanel::getFileName).orElse("无"), fxmlFileName.getFileName());
        }
        currentContentPanel = fxmlFileName;
        tabWindow.getChildren().clear();
        tabWindow.getChildren().add(view);
    }

    public void showFditorPanel(ActionEvent actionEvent) {
        loadOrGetView(ContentPanel.EDITORPANEL);
    }
}
