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

package tech.minediamond.vortex.ui.component;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.kordamp.ikonli.fluentui.FluentUiRegularAL;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.javafx.FontIcon;
import tech.minediamond.vortex.model.fileData.FileData;
import tech.minediamond.vortex.service.i18n.I18nService;

import java.util.function.Consumer;

public class SearchResultCard extends Control {

    private final ObjectProperty<Consumer<FileData>> onOpen = new SimpleObjectProperty<>();
    private final ObjectProperty<Consumer<FileData>> onRevealInFolder = new SimpleObjectProperty<>();
    private final ObjectProperty<Consumer<FileData>> onCopy = new SimpleObjectProperty<>();
    private final ObjectProperty<Consumer<FileData>> onOpenPathInTerminal = new SimpleObjectProperty<>();

    private final I18nService i18n;

    @Getter
    private final FileData result;

    @Inject
    public SearchResultCard(@Assisted FileData result, I18nService i18n) {
        this.result = result;
        this.i18n = i18n;
    }

    @Override
    protected Skin createDefaultSkin() {
        return new Skin(this);
    }

    public Consumer<FileData> getOnOpen() {
        return onOpen.get();
    }

    public void setOnOpen(Consumer<FileData> action) {
        onOpen.set(action);
    }

    public ObjectProperty<Consumer<FileData>> onOpenProperty() {
        return onOpen;
    }

    public Consumer<FileData> getOnRevealInFolder() {
        return onRevealInFolder.get();
    }

    public void setOnRevealInFolder(Consumer<FileData> action) {
        onRevealInFolder.set(action);
    }

    public ObjectProperty<Consumer<FileData>> onRevealInFolderProperty() {
        return onRevealInFolder;
    }

    public Consumer<FileData> getOnCopy() {
        return onCopy.get();
    }

    public void setOnCopy(Consumer<FileData> action) {
        onCopy.set(action);
    }

    public ObjectProperty<Consumer<FileData>> onCopyProperty() {
        return onCopy;
    }

    public Consumer<FileData> getOnOpenPathInTerminal() {
        return onOpenPathInTerminal.get();
    }

    public void setOnOpenPathInTerminal(Consumer<FileData> action) {
        onOpenPathInTerminal.set(action);
    }

    public ObjectProperty<Consumer<FileData>> onOpenPathInTerminalProperty() {
        return onOpenPathInTerminal;
    }

    public void open() {
        Consumer<FileData> c = getOnOpen();
        if (c != null) c.accept(result);
    }

    public void revealInFolder() {
        Consumer<FileData> c = getOnRevealInFolder();
        if (c != null) c.accept(result);
    }

    public void copy() {
        Consumer<FileData> c = getOnCopy();
        if (c != null) c.accept(result);
    }

    public void openPathInTerminal() {
        Consumer<FileData> c = getOnOpenPathInTerminal();
        if (c != null) c.accept(result);
    }

    private final class Skin extends SkinBase<SearchResultCard> {

        private final FontIcon openInFolderIcon = new FontIcon(FluentUiRegularAL.FOLDER_24);
        private final FontIcon openIcon = new FontIcon(FluentUiRegularMZ.OPEN_24);
        private final FontIcon copyPathIcon = new FontIcon(FluentUiRegularAL.COPY_24);
        private final FontIcon terminalIcon = new FontIcon(FluentUiRegularMZ.WINDOW_HORIZONTAL_20);

        HBox hBox = new HBox();
        VBox vbox = new VBox();

        /**
         * Constructor for all SkinBase instances.
         *
         * @param control The control for which this Skin should attach to.
         */
        protected Skin(SearchResultCard control) {
            super(control);

            hBox.getStyleClass().add("search-result-card");

            Label fileNameLabel = new Label(control.result.getFileName());
            Label filePathLabel = new Label(control.result.getFullPath());
            fileNameLabel.getStyleClass().add("search-result-name-label");
            filePathLabel.getStyleClass().add("search-result-file-path");

            Button openBtn = new Button();
            openBtn.setGraphic(openIcon);
            openBtn.visibleProperty().bind(hBox.hoverProperty());
            openBtn.managedProperty().bind(hBox.hoverProperty());
            openBtn.setOnAction(e -> getSkinnable().open());
            SimpleHoverTooltip.textProperty(openBtn).set(i18n.t("file.open.tip"));

            Button openInFolderBtn = new Button();
            openInFolderBtn.setGraphic(openInFolderIcon);
            openInFolderBtn.visibleProperty().bind(hBox.hoverProperty());
            openInFolderBtn.managedProperty().bind(hBox.hoverProperty());
            openInFolderBtn.setOnAction(e -> getSkinnable().revealInFolder());
            SimpleHoverTooltip.textProperty(openInFolderBtn).set(i18n.t("file.openInFolder.tip"));

            Button copyPathBtn = new Button();
            copyPathBtn.setGraphic(copyPathIcon);
            copyPathBtn.visibleProperty().bind(hBox.hoverProperty());
            copyPathBtn.managedProperty().bind(hBox.hoverProperty());
            copyPathBtn.setOnAction(e -> getSkinnable().copy());
            SimpleHoverTooltip.textProperty(copyPathBtn).set(i18n.t("file.copyPath.tip"));

            Button openPathInTerminal = new Button();
            openPathInTerminal.setGraphic(terminalIcon);
            openPathInTerminal.visibleProperty().bind(hBox.hoverProperty());
            openPathInTerminal.managedProperty().bind(hBox.hoverProperty());
            openPathInTerminal.setOnAction(e -> getSkinnable().openPathInTerminal());
            SimpleHoverTooltip.textProperty(openPathInTerminal).set(i18n.t("file.openPathInTerminal.tip"));

            Region region = new Region();
            HBox.setHgrow(region, Priority.ALWAYS);

            vbox.getChildren().addAll(fileNameLabel, filePathLabel);
            hBox.getChildren().addAll(vbox, region, openBtn, openInFolderBtn, copyPathBtn, openPathInTerminal);
            getChildren().add(hBox);
        }
    }

}
