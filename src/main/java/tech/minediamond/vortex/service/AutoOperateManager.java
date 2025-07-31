package tech.minediamond.vortex.service;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoOperateManager {

    private static final Logger logger = LoggerFactory.getLogger(AutoOperateManager.class);

    public static void setAutoFocus(Stage stage, String nodeIdToFocus){
        stage.setOnShown(event -> {
            Scene scene = stage.getScene();
            if (scene == null) {
                logger.warn("No scene found");
                return;
            }

            //使用 lookup() 在场景中寻找目标节点
            // "#" 是CSS ID选择器的语法
            Node targetNode = scene.lookup("#" + nodeIdToFocus);

            if (targetNode != null) {
                targetNode.requestFocus();
            } else {
                logger.warn(" 在场景中未找到ID为{}的节点", nodeIdToFocus);
            }
        });
    }
}
