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


import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import com.google.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.AppConfig;

import java.util.List;

@Slf4j
public class EditorPanel {

    @FXML
    private RichTextArea richTextArea;
    @FXML
    private ToggleButton setLineNum;
    @FXML
    private ToggleButton setWarpButton;
    @FXML
    private TextField searchField;
    @FXML
    private Button findPreviousBtn;
    @FXML
    private Button findNextBtn;
    @FXML
    private Label showIndexLabel;
    @FXML
    private Button saveTextBtn;

    AppConfig config;


    @Inject
    public EditorPanel(AppConfig config) {
        this.config = config;
    }

    public void initialize() {

        //if (config.wordWrapProperty().getValue()) codeArea.setWrapText(true);

        SimpleHoverTooltip.textProperty(setLineNum).bind(Bindings.when(config.showLineNumProperty()).then("显示行号：开").otherwise("显示行号：关"));
        SimpleHoverTooltip.textProperty(setWarpButton).bind(Bindings.when(config.wordWrapProperty()).then("自动换行：开").otherwise("自动换行：关"));
        //SimpleHoverTooltip.textProperty(showIndexLabel).bind(Bindings.format("所有匹配数量：%d\n当前位于：%d", Bindings.size(positions), positionIndex.add(1)));

        //findPreviousBtn.disableProperty().bind(Bindings.size(positions).lessThanOrEqualTo(0));
        //findNextBtn.disableProperty().bind(Bindings.size(positions).lessThanOrEqualTo(0));

        setWarpButton.selectedProperty().bindBidirectional(config.wordWrapProperty());

        setLineNum.selectedProperty().bindBidirectional(config.showLineNumProperty());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            //getSearchPosition(newValue);
        });


        //showIndexLabel.textProperty().bind(Bindings.format("%d/%d", positionIndex.add(1), Bindings.size(positions)));
        showIndexLabel.setText("0/0");

        saveTextBtn.setOnAction(event -> {

        });

        String text = "Hello RTA";
        TextDecoration textDecoration = TextDecoration.builder().presets()
                .fontFamily("Arial")
                .fontSize(20)
                .foreground("red")
                .build();
        ParagraphDecoration paragraphDecoration = ParagraphDecoration.builder().presets().build();
        DecorationModel decorationModel = new DecorationModel(0, text.length(), textDecoration, paragraphDecoration);
        Document document = new Document(text, List.of(decorationModel), text.length());
        richTextArea.getActionFactory().open(document).execute(new ActionEvent());

    }

    public void findPrevious(ActionEvent actionEvent) {

    }

    public void findNext(ActionEvent actionEvent) {

    }


}
