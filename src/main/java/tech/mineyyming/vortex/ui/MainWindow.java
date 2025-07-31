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
import tech.mineyyming.vortex.service.AutoOperateManager;
import tech.mineyyming.vortex.service.BindingUtils;
import tech.mineyyming.vortex.service.ShowStageListener;
import tech.mineyyming.vortex.service.WindowAnimator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MainWindow {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MainWindow.class);
    AppConfig config = AppConfigManager.getInstance();

    private static ContentPanel currentContentPanel;

    @FXML
    private AnchorPane mainWindow;
    @FXML
    private AnchorPane tabWindow;
    @FXML
    private ToggleButton pinBtn;
    @FXML
    private ToggleGroup mainToggleGroup;
    @FXML
    private ToggleButton quickEditBtn;
    @FXML
    private Button themeSwitchBtn;
    @FXML
    private Button hideWindowBtn;
    //缓存已经加载的视图
    private Map<String, Parent> viewCache = new HashMap<>();

    private double xOffset = 0;
    private double yOffset = 0;

    Stage stage;

    public void initialize() {

        handleDragWindow();
        initUIComponent();

        loadOrGetView(ContentPanel.EDITORPANEL);
        mainToggleGroup.selectToggle(quickEditBtn);
    }

    //鼠标拖拽移动窗口功能
    public void handleDragWindow(){
        mainWindow.setOnMousePressed(event -> {
            System.out.println("主界面被点击了");
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        mainWindow.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            double y = event.getScreenY() - yOffset < Screen.getPrimary().getVisualBounds().getMaxY() - 50 ? event.getScreenY() - yOffset : Screen.getPrimary().getVisualBounds().getMaxY() - 50;
            stage.setY(y);
        });
    }

    public void initUIComponent(){
        pinBtn.setSelected(!config.getAutoCloseOnFocusLoss());
        BindingUtils.bindBidirectionalInverse(pinBtn.selectedProperty(), config.autoCloseOnFocusLossProperty());
        SimpleHoverTooltip.textProperty(pinBtn).bind(Bindings.when(config.autoCloseOnFocusLossProperty()).then("未固定").otherwise("已固定"));
        SimpleHoverTooltip.setText(hideWindowBtn,"隐藏窗口(Ctrl+Space)");

        SimpleHoverTooltip.textProperty(themeSwitchBtn).bind(Bindings.createStringBinding(() -> {
            Theme theme = config.getTheme();
            return switch (theme) {
                case LIGHT -> "主题：亮色";
                case DARK -> "主题：暗色";
            };
        }, config.themeProperty()));

        themeSwitchBtn.setOnAction(event -> {
            Theme theme = config.getTheme();
            if (theme == Theme.LIGHT) {
                config.setTheme(Theme.DARK);
            } else {
                config.setTheme(Theme.LIGHT);
            }
        });

        hideWindowBtn.setOnAction(event -> {
            WindowAnimator.hideWindow(stage);
        });

        mainToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                mainToggleGroup.selectToggle(oldValue);
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setupStageProperties() {
        stage.setTitle("Vortex");
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon_x510.png")));
        AutoOperateManager.setAutoFocus(stage, "searchField");
    }

    //  这个方法将在 stage 被设置后，由 Main 类手动调用
    public void setupGlobalKeyListener() {
        // 确保 stage 不是 null
        if (this.stage == null) {
            logger.error("在设置 Stage 之前调用了 setupGlobalKeyListener");
            return;
        }
        logger.info("setupGlobalKeyListener() called. Stage is now available.");

        try {
            // 注册全局钩子
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            logger.error("注册全局钩子时出现问题。", ex);
            Platform.exit();
        }

        // 现在 stage 肯定不是 null，可以安全地创建监听器了
        ShowStageListener showStageListener = new ShowStageListener(this.stage);
        GlobalScreen.addNativeKeyListener(showStageListener);
        logger.info("全局按键监听器已成功设置！");
    }

    public void setupWindowListeners() {

        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && config.getAutoCloseOnFocusLoss()) {
                WindowAnimator.hideWindow(stage);
            }
        });

        stage.iconifiedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                stage.setIconified(false);
            }
        });

        stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                WindowAnimator.hideWindow(stage);
            }
        });
    }

    private void loadOrGetView(ContentPanel fxmlFileName) {
        if (fxmlFileName == currentContentPanel) {
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

                // 让新视图充满 tabWindow
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);
                logger.info("原页面：{}，新页面：{}，从文件加载新页面", Optional.ofNullable(currentContentPanel).map(ContentPanel::getFileName).orElse("无"), fxmlFileName.getFileName());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            logger.info("原页面：{}，新页面：{}，从缓存加载新页面", Optional.ofNullable(currentContentPanel).map(ContentPanel::getFileName).orElse("无"), fxmlFileName.getFileName());
        }
        currentContentPanel = fxmlFileName;
        //更新tabWindow
        tabWindow.getChildren().setAll(view);
    }

    public void showEditorPanel(ActionEvent actionEvent) {
        loadOrGetView(ContentPanel.EDITORPANEL);
    }

    public void showSettingPanel(ActionEvent actionEvent) {
        loadOrGetView(ContentPanel.SETTINGPANEL);
    }
}
