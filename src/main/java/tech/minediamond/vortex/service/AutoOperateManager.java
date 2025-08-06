package tech.minediamond.vortex.service;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class AutoOperateManager {

    public static void setAutoFocus(Stage stage, String nodeIdToFocus){
        stage.setOnShown(event -> {
            Scene scene = stage.getScene();
            if (scene == null) {
                log.warn("No scene found");
                return;
            }

            //使用 lookup() 在场景中寻找目标节点
            // "#" 是CSS ID选择器的语法
            Node targetNode = scene.lookup("#" + nodeIdToFocus);

            if (targetNode != null) {
                targetNode.requestFocus();
            } else {
                log.warn(" 在场景中未找到ID为{}的节点", nodeIdToFocus);
            }
        });
    }
}
