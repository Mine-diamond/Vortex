package tech.mineyyming.vortex.ui;

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

    private Boolean isShowLineNum = true;
    private Boolean isWarp = true;

    // 1. 定义回调函数的引用
    private Runnable onToggleSizeRequest;



    public void initialize(){

        textEdit.setParagraphGraphicFactory(LineNumberFactory.get(textEdit));
        textEdit.setWrapText(true);

        setLineNum.setOnAction(event -> {
            if (isShowLineNum){
                textEdit.setParagraphGraphicFactory(null);
                SimpleHoverTooltip.setText(setLineNum,"显示行号：关");
                isShowLineNum = false;
            } else {
                textEdit.setParagraphGraphicFactory(LineNumberFactory.get(textEdit));
                SimpleHoverTooltip.setText(setLineNum,"显示行号：开");
                isShowLineNum = true;
            }
        });

        setWarpButton.setOnAction(event -> {
            if (isWarp){
                textEdit.setWrapText(false);
                SimpleHoverTooltip.setText(setWarpButton,"自动换行：关");
                isWarp = false;
            } else {
                textEdit.setWrapText(true);
                SimpleHoverTooltip.setText(setWarpButton,"自动换行：开");
                isWarp = true;
            }
        });
    }
}
