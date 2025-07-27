package tech.mineyyming.vortex.ui;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;
import tech.mineyyming.vortex.model.AppConfig;
import tech.mineyyming.vortex.model.AppConfigManager;
import tech.mineyyming.vortex.model.ContentPanel;
import tech.mineyyming.vortex.model.Theme;
import tech.mineyyming.vortex.service.BindingUtils;
import tech.mineyyming.vortex.service.ShowStageListener;
import tech.mineyyming.vortex.service.WindowAnimator;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MainWindow {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MainWindow.class);
    private static ContentPanel currentContentPanel;

    AppConfig config = AppConfigManager.getInstance();

    @FXML
    private AnchorPane mainWindow;
    @FXML
    private AnchorPane tabWindow;
    @FXML
    private ToggleButton pinBtn;
    @FXML
    private Button exitBtn;
    @FXML
    private ToggleGroup mainToggleGroup;
    @FXML
    private ToggleButton quickEditBtn;
    @FXML
    private Button themeSwitchBtn;
    //缓存已经加载的视图
    private Map<String, Parent> viewCache = new HashMap<>();

    private double xOffset = 0;
    private double yOffset = 0;

    Stage stage;

    public void initialize() {

        mainWindow.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.ESCAPE) {
                //stage.hide();
                WindowAnimator.hideWindow(stage);
            }
        });

        mainWindow.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        mainWindow.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset);
                double y = event.getScreenY() - yOffset < Screen.getPrimary().getVisualBounds().getMaxY() - 50 ? event.getScreenY() - yOffset : Screen.getPrimary().getVisualBounds().getMaxY() - 50;
                stage.setY(y);
        });

        //pinBtn.selectedProperty().bindBidirectional(config.autoCloseOnFocusLossProperty());
        pinBtn.setSelected(!config.getAutoCloseOnFocusLoss());
        BindingUtils.bindBidirectionalInverse(pinBtn.selectedProperty(),config.autoCloseOnFocusLossProperty());
        SimpleHoverTooltip.textProperty(pinBtn).bind(Bindings.when(config.autoCloseOnFocusLossProperty()).then("失焦隐藏：开启").otherwise("失焦隐藏：关闭"));

        SimpleHoverTooltip.textProperty(themeSwitchBtn).bind(Bindings.createStringBinding(()->{
            Theme theme = config.getTheme();
            return switch(theme) {
                case LIGHT -> "主题：亮色";
                case DARK -> "主题：暗色";
            };
        },config.themeProperty()));
        themeSwitchBtn.setOnAction(event -> {
            Theme theme = config.getTheme();
            if(theme == Theme.LIGHT){
                config.setTheme(Theme.DARK);
            } else {
                config.setTheme(Theme.LIGHT);
            }
        });

        exitBtn.setOnAction(event -> {
            if(stage.isShowing()) {
                WindowAnimator.hideWindow(stage,Platform::exit);
            }
        });

        mainToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null) {
                mainToggleGroup.selectToggle(oldValue);
            }
        });

        loadOrGetView(ContentPanel.EDITORPANEL);
        mainToggleGroup.selectToggle(quickEditBtn);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setPrimaryStage(){
        stage.setTitle("Vortex");
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon_x510.png")));
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
            if(!newValue && config.getAutoCloseOnFocusLoss()) {
                WindowAnimator.hideWindow(stage);
            }
        });

        stage.iconifiedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                stage.setIconified(false);
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/mineyyming/vortex/ui/" + fileName));
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
