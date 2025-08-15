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

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;

/**
 * 一个管理器类，用于为任何 Node 安装和管理一个悬浮提示 PopOver。
 * 支持通过 FXML 附加属性进行声明式使用。
 */
public class HoverPopOver {

    private static final Object HOVER_POPOVER_KEY = new Object();
    private static final double GAP_FROM_OWNER = 5.0;

    private final Node owner;
    private final PopOver popOver;
    private final Label textLabel;
    private final EventHandler<MouseEvent> enterHandler;
    private final EventHandler<MouseEvent> exitHandler;



    private HoverPopOver(Node owner, String tooltipText) {
        this.owner = owner;
        this.popOver = new PopOver();
        this.textLabel = new Label(tooltipText);
        this.textLabel.setTextFill(Color.WHITE);

        StackPane contentPane = new StackPane(textLabel);
        //popOver.setAutoFix(false);
        popOver.setArrowIndent(0);
        contentPane.setPadding(new Insets(1, 2, 1, 2));
        contentPane.setStyle("-fx-background-color: #424242; -fx-background-radius: 2;");
        this.popOver.getRoot().setFocusTraversable(false);                    // 🎯 不参与焦点遍历
        this.popOver.getContentNode().setFocusTraversable(false);   // 🎯 内容也不获取焦点
        this.popOver.getRoot().setMouseTransparent(true);
        this.popOver.getContentNode().setMouseTransparent(true);

        this.popOver.setContentNode(contentPane);
        this.popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_CENTER);
        this.popOver.setDetachable(false);
        this.popOver.setHeaderAlwaysVisible(false);
        this.popOver.setCornerRadius(2);
        this.popOver.setAutoHide(true);
        this.popOver.setHideOnEscape(true);

        //始终显示在控件上方
        this.enterHandler = event -> {
            if (!this.popOver.isShowing()) {
                Bounds ownerBounds = this.owner.localToScreen(this.owner.getBoundsInLocal());
                double targetX = ownerBounds.getCenterX();
                double targetY = ownerBounds.getMinY() - GAP_FROM_OWNER;
                this.popOver.show(this.owner, targetX, targetY);
                System.out.println("显示位置：" + targetX + "," + targetY);
            }
        };

        this.exitHandler = event -> {this.popOver.hide();System.out.println("鼠标已移出控件");};

        this.owner.addEventHandler(MouseEvent.MOUSE_ENTERED, this.enterHandler);
        this.owner.addEventHandler(MouseEvent.MOUSE_EXITED, this.exitHandler);
    }

    public void setText(String newText) {
        this.textLabel.setText(newText);
    }

    public void uninstall() {
        this.popOver.hide();
        this.owner.removeEventHandler(MouseEvent.MOUSE_ENTERED, this.enterHandler);
        this.owner.removeEventHandler(MouseEvent.MOUSE_EXITED, this.exitHandler);
        this.owner.getProperties().remove(HOVER_POPOVER_KEY);
    }

    // --- 核心逻辑：安装或更新实例 ---
    private static HoverPopOver installOrUpdate(Node owner, String text) {
        HoverPopOver instance = (HoverPopOver) owner.getProperties().get(HOVER_POPOVER_KEY);
        if (instance != null) {
            instance.setText(text);
            return instance;
        } else {
            HoverPopOver newInstance = new HoverPopOver(owner, text);
            owner.getProperties().put(HOVER_POPOVER_KEY, newInstance);
            return newInstance;
        }
    }

    private static void uninstall(Node owner) {
        HoverPopOver instance = (HoverPopOver) owner.getProperties().get(HOVER_POPOVER_KEY);
        if (instance != null) {
            instance.uninstall();
        }
    }

    // ========================================================================
    // == FXML 附加属性支持 (THE MAGIC HAPPENS HERE) ==
    // ========================================================================

    /**
     * FXML 附加属性的 setter 方法。
     * 当 FXML 解析器遇到 <Node HoverPopOver.text="..."/> 时，会调用此方法。
     * @param node  应用此属性的节点 (例如 Button)
     * @param text  来自 FXML 的文本值
     */
    public static void setText(Node node, String text) {
        if (text == null || text.trim().isEmpty()) {
            // 如果文本为空，则卸载任何已存在的 PopOver
            uninstall(node);
        } else {
            // 否则，安装或更新 PopOver
            installOrUpdate(node, text);
        }
    }

    /**
     * FXML 附加属性的 getter 方法。
     * 主要用于 Scene Builder 等工具，或者在代码中读取值。
     * @param node 目标节点
     * @return 附加在该节点上的 PopOver 文本，如果没有则返回 null
     */
    public static String getText(Node node) {
        HoverPopOver instance = (HoverPopOver) node.getProperties().get(HOVER_POPOVER_KEY);
        return (instance != null) ? instance.textLabel.getText() : null;
    }
}

