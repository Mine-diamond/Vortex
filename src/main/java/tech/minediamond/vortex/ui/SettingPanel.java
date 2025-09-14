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

package tech.minediamond.vortex.ui;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.ToggleSwitch;
import tech.minediamond.vortex.model.AppConfig;
import tech.minediamond.vortex.model.SupportedLocales;
import tech.minediamond.vortex.service.StageProvider;
import tech.minediamond.vortex.service.I18nService;
import tech.minediamond.vortex.service.WindowAnimator;
import tech.minediamond.vortex.service.interfaces.IAutoStartService;

import java.awt.*;
import java.net.URI;

@Slf4j
public class SettingPanel {

    private WindowAnimator windowAnimator;

    @FXML
    private ComponentList settingList;
    @FXML
    private Button exitBtn;
    @FXML
    private Button openOfficialWebsiteBtn;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ComboBox<String> showPlaceComboBox;
    @FXML
    private ComboBox<String> userLanguageComBox;
    @FXML
    private ToggleSwitch autoStartOnBootToggleSwitch;

    private final AppConfig appConfig;
    private final StageProvider stageProvider;
    private final I18nService i18n;
    private final IAutoStartService autoStartService;

    StringConverter<Boolean> showPlaceComboBoxConverter;
    StringConverter<SupportedLocales> supportedLocalesConverter;

    @Inject
    public SettingPanel(WindowAnimator windowAnimator, AppConfig appConfig, StageProvider stageProvider, I18nService i18n, IAutoStartService autoStartService) {
        this.windowAnimator = windowAnimator;
        this.appConfig = appConfig;
        this.stageProvider = stageProvider;
        this.i18n = i18n;
        this.autoStartService = autoStartService;
    }

    public void initialize() {
        showPlaceComboBoxConverter = new StringConverter<>() {
            @Override
            public String toString(Boolean value) {
                // 将 Boolean 翻译成 String
                return value != null && value ? i18n.t("setting.showWindow.comBox.center") : i18n.t("setting.showWindow.comBox.onLeft");
            }

            @Override
            public Boolean fromString(String string) {
                // 将 String 翻译回 Boolean
                return i18n.t("setting.showWindow.comBox.center").equals(string);
            }
        };

        supportedLocalesConverter = new StringConverter<>() {

            @Override
            public String toString(SupportedLocales supportedLocales) {
                if (supportedLocales == null) {
                    return ""; // 如果对象为空，返回空字符串
                }

                // 根据枚举名，构造 properties 文件中的 key
                String key = supportedLocales.getI18nKey();

                // 从 ResourceBundle 中获取对应的字符串值
                // 如果找不到，为了程序不崩溃，可以返回一个默认值，比如 key 本身
                return i18n.t(key);
            }

            @Override
            public SupportedLocales fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }
                // 遍历所有枚举值，找到显示名称匹配的那个
                for (SupportedLocales locale : SupportedLocales.values()) {
                    if (toString(locale).equals(string)) {
                        return locale;
                    }
                }
                // 如果找不到匹配项，返回 null 或抛出异常
                return null;
            }
        };

        userLanguageComBox.setItems(FXCollections.observableArrayList(
                i18n.t("lang.auto"),
                i18n.t("lang.en"),
                i18n.t("lang.zh_CN"),
                i18n.t("lang.zh_TW")
        ));

        showPlaceComboBox.setItems(FXCollections.observableArrayList(
                i18n.t("setting.showWindow.comBox.center"),
                i18n.t("setting.showWindow.comBox.onLeft")
        ));

        Bindings.bindBidirectional(showPlaceComboBox.valueProperty(), appConfig.ifCenterOnScreenProperty(), showPlaceComboBoxConverter);
        Bindings.bindBidirectional(userLanguageComBox.valueProperty(),appConfig.userLocalesProperty(), supportedLocalesConverter);
        Bindings.bindBidirectional(autoStartOnBootToggleSwitch.selectedProperty(),autoStartService.autoStartEnabledProperty());
    }


    /**
     * 这是 {@link #exitBtn} 的默认触发动作
     * @param actionEvent
     */
    public void exitBtnAction(ActionEvent actionEvent) {
        if (stageProvider.getStage().isShowing()) {
            windowAnimator.hideWindow(stageProvider.getStage(), Platform::exit);
        }
    }

    /**
     * 这是 {@link #openOfficialWebsiteBtn} 的默认触发动作
     * @param actionEvent
     */
    public void openWebsiteAction(ActionEvent actionEvent) {
        String url = "https://github.com/Mine-diamond/Vortex";

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (Exception e) {
                log.error("打开浏览器出错：" + e.getMessage());
            }
        }
    }


}
