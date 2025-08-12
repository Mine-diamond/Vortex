package tech.minediamond.vortex.ui;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.AppConfig;
import tech.minediamond.vortex.service.WindowAnimator;

import java.awt.*;
import java.net.URI;

@Slf4j
public class SettingPanel {

    private static WindowAnimator windowAnimator;

    @FXML
    private VBox settingList;
    @FXML
    private Button exitBtn;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ComboBox<String> showPlaceComboBox;

    private static Stage stage;
    private static AppConfig appConfig;

    StringConverter<Boolean> converter;

    @Inject
    public SettingPanel(WindowAnimator windowAnimator, AppConfig appConfig) {
        this.windowAnimator = windowAnimator;
        this.appConfig = appConfig;
    }

    public void initialize() {
        converter = new StringConverter<>() {
            @Override
            public String toString(Boolean value) {
                // 将 Boolean 翻译成 String
                return value != null && value ? "居中显示" : "在隐藏时的位置显示";
            }

            @Override
            public Boolean fromString(String string) {
                // 将 String 翻译回 Boolean
                return "居中显示".equals(string);
            }
        };

        Bindings.bindBidirectional(showPlaceComboBox.valueProperty(), appConfig.ifCenterOnScreenProperty(), converter);
    }

    private Stage getStage() {
        if (stage == null) {
            stage = (Stage) settingList.getScene().getWindow();
        }
        return stage;
    }

    public void exitBtnAction(ActionEvent actionEvent) {
        if (getStage().isShowing()) {
            windowAnimator.hideWindow(getStage(), Platform::exit);
        }
    }

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
