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

package tech.minediamond.vortex.service.ui;

import com.google.inject.Inject;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutoOperateService {

    @Inject
    public AutoOperateService() {}

    public void setAutoFocus(Stage stage, String nodeIdToFocus){
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
