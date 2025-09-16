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

package tech.minediamond.vortex.ui.controller;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.fluentui.FluentUiFilledMZ;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.minediamond.vortex.model.appConfig.AppConfig;
import tech.minediamond.vortex.model.ui.ContentPanel;
import tech.minediamond.vortex.model.ui.Theme;
import tech.minediamond.vortex.service.i18n.I18nService;
import tech.minediamond.vortex.service.ui.*;
import tech.minediamond.vortex.ui.component.SimpleHoverTooltip;
import tech.minediamond.vortex.util.BindingUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static tech.minediamond.vortex.model.ui.Theme.*;

@Slf4j
public class MainWindow {
    private final AppConfig config;
    private final Injector injector;
    private final WindowAnimator windowAnimator;
    private final I18nService i18n;
    private final AutoOperateService autoOperateService;

    @FXML
    private AnchorPane mainWindow;
    @FXML
    private AnchorPane tabWindow;
    @FXML
    private TextField searchField;
    @FXML
    private ToggleButton pinBtn;
    @FXML
    private ToggleGroup mainToggleGroup;
    @FXML
    private ToggleButton quickEditBtn;
    @FXML
    private ToggleButton searchBtn;
    @FXML
    private ToggleButton settingBtn;
    @FXML
    private Button themeSwitchBtn;
    @FXML
    private Button hideWindowBtn;
    //缓存已经加载的视图
    private final Map<String, Parent> viewCache = new HashMap<>();
    private final ObjectProperty<ContentPanel> currentContentPanelProperty = new SimpleObjectProperty<>(ContentPanel.EDITOR_PANEL);
    private SearchPanel searchPanel;

    private double xOffset = 0;
    private double yOffset = 0;

    private static final FontIcon pinIcon = new FontIcon(FluentUiFilledMZ.PIN_20);
    private static final FontIcon unpinIcon = new FontIcon(FluentUiRegularMZ.PIN_20);
    private static final FontIcon lightThemeIcon = new FontIcon(FluentUiRegularMZ.WEATHER_SUNNY_24);
    private static final FontIcon darkThemeIcon = new FontIcon(FluentUiRegularMZ.WEATHER_MOON_24);
    private static final FontIcon autoThemeIcon = new FontIcon(FluentUiRegularAL.ARROW_REPEAT_ALL_24);

    Stage stage;

    @Inject
    public MainWindow(AppConfig config, Injector injector, WindowAnimator windowAnimator, StageProvider stageProvider, I18nService i18nService, AutoOperateService autoOperateService) {
        this.config = config;
        this.injector = injector;
        this.windowAnimator = windowAnimator;
        this.i18n = i18nService;
        this.autoOperateService = autoOperateService;

        this.stage = stageProvider.getStage();


        setupStageProperties();
        setupGlobalKeyListener();
        setupWindowListeners();
    }

    public void initialize() {

        handleDragWindow();
        initUIComponent();

        loadOrGetView(currentContentPanelProperty.get());
        mainToggleGroup.selectToggle(quickEditBtn);
    }

    /**
     * 鼠标拖拽移动窗口功能方法
     */
    public void handleDragWindow() {
        mainWindow.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        mainWindow.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            double y = Math.min(event.getScreenY() - yOffset, Screen.getPrimary().getVisualBounds().getMaxY() - 50);
            stage.setY(y);
        });
    }

    /**
     * 为mainWindows的各个组件提供基本的文本绑定点击事件绑定
     */
    public void initUIComponent() {
        pinBtn.setSelected(!config.getAutoCloseOnFocusLoss());
        BindingUtils.bindBidirectionalInverse(pinBtn.selectedProperty(), config.autoCloseOnFocusLossProperty());
        SimpleHoverTooltip.textProperty(pinBtn).bind(Bindings.when(config.autoCloseOnFocusLossProperty()).then(i18n.t("window.unpin")).otherwise(i18n.t("window.pin")));
        SimpleHoverTooltip.setText(hideWindowBtn, i18n.t("window.action.hide"));

        SimpleHoverTooltip.textProperty(themeSwitchBtn).bind(Bindings.createStringBinding(() -> {
            Theme theme = config.getTheme();
            return switch (theme) {
                case LIGHT -> i18n.t("main.theme.tooltip.light");
                case DARK -> i18n.t("main.theme.tooltip.dark");
                case AUTO -> i18n.t("main.theme.tooltip.auto");
            };
        }, config.themeProperty()));

        themeSwitchBtn.setOnAction(event -> config.setTheme(switch (config.getTheme()) {
                case LIGHT -> DARK;
                case DARK  -> AUTO;
                case AUTO  -> LIGHT;
            }));

        hideWindowBtn.setOnAction(event -> {
            windowAnimator.hideWindow(stage);
        });

        pinBtn.graphicProperty().bind(Bindings.when(pinBtn.selectedProperty()).then(pinIcon).otherwise(unpinIcon));

        themeSwitchBtn.graphicProperty().bind(Bindings.createObjectBinding(() -> switch (config.getTheme()) {
            case LIGHT -> lightThemeIcon;
            case DARK -> darkThemeIcon;
            case AUTO -> autoThemeIcon;
        }, config.themeProperty()));

        mainToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                mainToggleGroup.selectToggle(oldValue);
            }
        });

        currentContentPanelProperty.addListener((observable, oldValue, newValue) -> {
            loadOrGetView(newValue);
            switch (newValue) {
                case EDITOR_PANEL -> mainToggleGroup.selectToggle(quickEditBtn);
                case SETTING_PANEL -> mainToggleGroup.selectToggle(settingBtn);
                case SEARCH_PANEL -> mainToggleGroup.selectToggle(searchBtn);
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                currentContentPanelProperty.set(ContentPanel.SEARCH_PANEL);
                searchPanel.search(newValue);
            } else if (newValue.isEmpty()) {
                searchPanel.searchClear();
            }
        });
    }


    /**
     * 设置主窗口的属性
     */
    public void setupStageProperties() {
        stage.setTitle("Vortex");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon_x510.png")));
        autoOperateService.setAutoFocus(stage, "searchField");
    }

    //  这个方法将在 stage 被设置后，由 Main 类手动调用
    public void setupGlobalKeyListener() {
        // 确保 stage 不是 null
        if (this.stage == null) {
            log.error("在设置 Stage 之前调用了 setupGlobalKeyListener");
            return;
        }
        log.info("setupGlobalKeyListener() called. Stage is now available.");

        try {
            // 注册全局钩子
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            log.error("注册全局钩子时出现问题。", ex);
            Platform.exit();
        }

        ShowStageListener showStageListener = injector.getInstance(ShowStageListenerFactory.class).create(stage);
        GlobalScreen.addNativeKeyListener(showStageListener);
        log.info("全局按键监听器已成功设置。");
    }

    public void setupWindowListeners() {

        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && config.getAutoCloseOnFocusLoss()) {
                windowAnimator.hideWindow(stage);
            }
        });

        stage.iconifiedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                stage.setIconified(false);
            }
        });

        stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                windowAnimator.hideWindow(stage);
            }
        });
    }

    /**
     * 加载面板到区域
     *
     * @param fxmlFileName 文件名
     */
    private void loadOrGetView(ContentPanel fxmlFileName) {
        String fileName = fxmlFileName.getFileName();
        Parent view = viewCache.get(fileName);

        if (view == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/tech/minediamond/vortex/ui/" + fileName));
                loader.setControllerFactory(injector::getInstance);
                loader.setResources(injector.getInstance(I18nService.class).getResourceBundle());
                view = loader.load();
                viewCache.put(fileName, view); // 加载后放入缓存
                if (fxmlFileName == ContentPanel.SEARCH_PANEL) {
                    searchPanel = loader.getController();
                }

                // 让新视图充满 tabWindow
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);
                log.info("加载新页面：{}，从文件加载新页面", fxmlFileName.getFileName());
            } catch (IOException e) {
                log.error("加载 {} 页面出现错误: ", fileName, e);
                return;
            }
        } else {
            log.info("加载新页面：{}，从缓存加载新页面",  fxmlFileName.getFileName());
        }
        //更新tabWindow
        tabWindow.getChildren().setAll(view);
    }

    public void showEditorPanel(ActionEvent actionEvent) {
        currentContentPanelProperty.set(ContentPanel.EDITOR_PANEL);
    }

    public void showSettingPanel(ActionEvent actionEvent) {
        currentContentPanelProperty.set(ContentPanel.SETTING_PANEL);
    }

    public void showSearchPanel(ActionEvent actionEvent) {
        currentContentPanelProperty.set(ContentPanel.SEARCH_PANEL);
    }
}
