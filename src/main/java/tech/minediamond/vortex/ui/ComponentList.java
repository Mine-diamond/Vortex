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

import javafx.beans.DefaultProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.function.Function;

/**
 * 自定义的 JavaFX 控件，用于将一组不同的 {@link Node} 以垂直列表的形式展示。
 * <p>
 * 这个控件的核心功能是为列表中的每个节点（Node）应用统一的容器样式，并能够通过 CSS
 * 伪类 (pseudo-class) 对列表中的第一个和最后一个元素应用特殊样式（例如，圆角）。
 * 这使得创建外观统一的组件分组变得非常容易，比如设置面板中的条目。
 *
 * @author Mine-diamond
 * @see Skin
 * @see VBox
 */
@DefaultProperty("content")
public class ComponentList extends Control {

    @Getter
    private final ObservableList<Node> content = FXCollections.observableArrayList();

    public ComponentList() {
        // 设置控件的默认样式类
        getStyleClass().add("component-list");
    }

    public void addNode(Node node) {
        content.add(node);
    }

    public void removeNode(Node node) {
        content.remove(node);
    }

    public int size(){
        return content.size();
    }

    // 创建并返回此控件的默认皮肤（Skin）
    @Override
    protected Skin createDefaultSkin() {
        return new Skin(this);
    }

    /**
     * 返回用户代理样式表的路径。
     * 在这个实现中，样式将在应用程序级别的主 CSS 文件中定义，因此返回 null。
     *
     * @return null
     */
    @Override
    public String getUserAgentStylesheet() {
        return null;
    }

    private static final class Skin extends SkinBase<ComponentList> {

        private static final PseudoClass PSEUDO_CLASS_FIRST = PseudoClass.getPseudoClass("first");// 标记列表中第一个节点
        private static final PseudoClass PSEUDO_CLASS_LAST = PseudoClass.getPseudoClass("last");// 标记列表中之后一个节点

        /**
         * 皮肤的构造函数。
         * <p>
         * 它将 {@code ComponentList} 的 {@code content}列表中的每个节点包装在一个 {@link StackPane} 中，然后将这些 StackPane 垂直排列在一个 {@link VBox} 中。
         *
         * @param control 与此皮肤关联的 ComponentList 控件。
         */
        public Skin(ComponentList control) {
            super(control);
            // 获取控件的原始节点列表
            ObservableList<Node> content = control.content;

            //创建一个映射列表，将原始列表中的每个 Node 映射（包装）到一个新的 StackPane 中，并添加到vBox的内容中
            ObservableList<StackPane> stackPaneContent = new MappedList<>(content, nodeToStackPaneMapper);
            VBox vBox = new VBox();
            Bindings.bindContent(vBox.getChildren(), stackPaneContent);

            // 动态更新第一个和最后一个元素的伪类状态
            ObjectBinding<StackPane> firstItem = Bindings.valueAt(stackPaneContent, 0);
            firstItem.addListener((observable, oldValue, newValue) -> {
                if (newValue != null)
                    newValue.pseudoClassStateChanged(PSEUDO_CLASS_FIRST, true);
                if (oldValue != null)
                    oldValue.pseudoClassStateChanged(PSEUDO_CLASS_FIRST, false);
            });

            ObjectBinding<StackPane> lastItem = Bindings.valueAt(stackPaneContent, Bindings.subtract(Bindings.size(stackPaneContent), 1));
            lastItem.addListener((observable, oldValue, newValue) -> {
                if (newValue != null)
                    newValue.pseudoClassStateChanged(PSEUDO_CLASS_LAST, true);
                if (oldValue != null)
                    oldValue.pseudoClassStateChanged(PSEUDO_CLASS_LAST, false);
            });
            if (firstItem.get() != null) {
                firstItem.get().pseudoClassStateChanged(PSEUDO_CLASS_FIRST, true);
            }
            if (lastItem.get() != null) {
                lastItem.get().pseudoClassStateChanged(PSEUDO_CLASS_LAST, true);
            }

            // 配置 VBox 布局并将其添加到皮肤的子节点中
            vBox.setFillWidth(true);
            vBox.setSpacing(0);
            getChildren().add(vBox);

        }

        /**
         * 这是一个函数式接口实现，定义了如何将一个 {@link Node} 转换为一个包装它的 {@link StackPane}。
         * 每个节点都会被这个函数处理。
         */
        static final Function<Node, StackPane> nodeToStackPaneMapper = node -> {
            StackPane stackPane = new StackPane(node);
            stackPane.getStyleClass().add("component-list-item");
            return stackPane;
        };
    }

}
