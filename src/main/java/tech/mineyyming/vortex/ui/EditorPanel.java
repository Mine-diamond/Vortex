package tech.mineyyming.vortex.ui;


import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class EditorPanel {

    @FXML
    private CodeArea textEdit;
    @FXML
    private Button setLineNum;
    @FXML
    private Button setWarpButton;

    private BooleanProperty isShowLineNum = new SimpleBooleanProperty(true);
    private BooleanProperty isWarp = new SimpleBooleanProperty(true);

    public void initialize(){

        textEdit.setParagraphGraphicFactory(LineNumberFactory.get(textEdit));
        textEdit.setWrapText(true);

        SimpleHoverTooltip.textProperty(setLineNum).bind(Bindings.when(isShowLineNum).then("显示行号：开").otherwise("显示行号：关"));
        SimpleHoverTooltip.textProperty(setWarpButton).bind(Bindings.when(isWarp).then("自动换行：开").otherwise("自动换行：关"));


        setLineNum.setOnAction(event -> {
            if (isShowLineNum.getValue()){
                textEdit.setParagraphGraphicFactory(null);
                //SimpleHoverTooltip.setText(setLineNum,"显示行号：关");
                isShowLineNum.setValue(false);
            } else {
                textEdit.setParagraphGraphicFactory(LineNumberFactory.get(textEdit));
                //SimpleHoverTooltip.setText(setLineNum,"显示行号：开");
                isShowLineNum.setValue(true);
            }
        });

        setWarpButton.setOnAction(event -> {
            if (isWarp.getValue()){
                textEdit.setWrapText(false);
                //SimpleHoverTooltip.setText(setWarpButton,"自动换行：关");
                isWarp.setValue(false);
            } else {
                textEdit.setWrapText(true);
                //SimpleHoverTooltip.setText(setWarpButton,"自动换行：开");
                isWarp.setValue(true);
            }
        });
    }
}
