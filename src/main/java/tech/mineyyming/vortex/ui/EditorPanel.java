package tech.mineyyming.vortex.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class EditorPanel {

    @FXML
    private CodeArea textEdit;
    @FXML
    private Button toggletextEditButton;
    @FXML
    private Button setLineNum;
    @FXML
    private Button setWarpButton;

    private Boolean isShowLineNum = true;
    private Boolean isWarp = true;

    // 1. 定义回调函数的引用
    private Runnable onToggleSizeRequest;

    // 2. 创建一个 public 方法，以便父控制器可以“注入”回调逻辑
    public void setOnToggleSizeRequest(Runnable onToggleSizeRequest) {
        this.onToggleSizeRequest = onToggleSizeRequest;
    }

    public void updateToggleButtonText(boolean isMaximized) {
        if (isMaximized) {
            // 这里可以用向右的箭头，或者文字 "缩小"
            toggletextEditButton.setText("➡");
            SimpleHoverTooltip.setText(toggletextEditButton,"点击缩小");
        } else {
            // 恢复为向左的箭头，或者文字 "放大"
            toggletextEditButton.setText("⬅");
            SimpleHoverTooltip.setText(toggletextEditButton,"点击展开");
        }
    }

    public void initialize(){

        textEdit.setParagraphGraphicFactory(LineNumberFactory.get(textEdit));
        textEdit.setWrapText(true);

        toggletextEditButton.setOnAction(event -> {
            // 3. 当按钮被点击时，检查回调是否已设置，如果设置了就执行它
            if (onToggleSizeRequest != null) {
                System.out.println("EditorPanel: Sending toggle size request to parent...");
                onToggleSizeRequest.run(); // 执行注入的回调
            } else {
                System.out.println("EditorPanel: Toggle button clicked, but no action is registered.");
            }
        });

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
