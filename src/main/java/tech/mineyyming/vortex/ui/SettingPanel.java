package tech.mineyyming.vortex.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.mineyyming.vortex.service.WindowAnimator;

import java.awt.*;
import java.net.URI;

public class SettingPanel {

    Logger logger = LoggerFactory.getLogger(SettingPanel.class);

    @FXML
    private VBox settingList;
    @FXML
    private Button exitBtn;
    @FXML
    private ScrollPane scrollPane;

    private static Stage stage;

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
            WindowAnimator.hideWindow(getStage(), Platform::exit);
        }
    }

    public void openWebsiteAction(ActionEvent actionEvent) {
        String url = "https://github.com/Mine-diamond/Vortex";

        if(Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (Exception e) {
                logger.error("打开浏览器出错：" + e.getMessage());
            }
        }
    }
}
