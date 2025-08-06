package tech.minediamond.vortex.ui;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
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

    private static Stage stage;

    @Inject
    public SettingPanel(WindowAnimator windowAnimator) {
        this.windowAnimator = windowAnimator;
    }

    public void initialize() {
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
