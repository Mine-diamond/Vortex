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
 * 为各个不同的node应用统一的样式，并以将node以列表形式展示
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

    //默认皮肤
    @Override
    protected Skin<?> createDefaultSkin() {
        return new Skin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return null;
    }

    private static final class Skin<T> extends SkinBase<ComponentList> {

        private static final PseudoClass PSEUDO_CLASS_FIRST = PseudoClass.getPseudoClass("first");
        private static final PseudoClass PSEUDO_CLASS_LAST = PseudoClass.getPseudoClass("last");

        private final VBox vBox = new VBox();

        /**
         * Constructor for all SkinBase instances.
         *
         * @param control The control for which this Skin should attach to.
         */
        protected Skin(ComponentList control) {
            super(control);
            ObservableList<Node> content = control.content;

            ObservableList<StackPane> stackPaneContent = new MappedList<>(content, nodeToStackPaneMapper);

            Bindings.bindContent(vBox.getChildren(), stackPaneContent);

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

            vBox.setFillWidth(true);
            vBox.setSpacing(0);
            getChildren().add(vBox);

        }

        static final Function<Node, StackPane> nodeToStackPaneMapper = node -> {
            StackPane stackPane = new StackPane(node);
            stackPane.getStyleClass().add("component-list-item");
            return stackPane;
        };
    }

}
