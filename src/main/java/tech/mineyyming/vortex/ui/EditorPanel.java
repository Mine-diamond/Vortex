package tech.mineyyming.vortex.ui;


import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EditorPanel {

    Logger logger = LoggerFactory.getLogger(EditorPanel.class);

    @FXML
    private CodeArea textEdit;
    @FXML
    private Button setLineNum;
    @FXML
    private Button setWarpButton;
    @FXML
    private TextField searchField;
    @FXML
    private Button findPreviousBtn;
    @FXML
    private Button findNextBtn;
    @FXML
    private Label showIndexLabel;

    private BooleanProperty isShowLineNum = new SimpleBooleanProperty(true);
    private BooleanProperty isWarp = new SimpleBooleanProperty(true);

    private ObservableList<Integer> allIndex = FXCollections.observableArrayList();
    private IntegerProperty currentIndex = new SimpleIntegerProperty(-1);
    private StringProperty foundIndexLabelText = new SimpleStringProperty("");

    public void initialize(){

        textEdit.setParagraphGraphicFactory(LineNumberFactory.get(textEdit));
        textEdit.setWrapText(true);

        SimpleHoverTooltip.textProperty(setLineNum).bind(Bindings.when(isShowLineNum).then("显示行号：开").otherwise("显示行号：关"));
        SimpleHoverTooltip.textProperty(setWarpButton).bind(Bindings.when(isWarp).then("自动换行：开").otherwise("自动换行：关"));
        SimpleHoverTooltip.textProperty(showIndexLabel).bind(Bindings.format("所有匹配数量：%d\n当前位于：%d",Bindings.size(allIndex),currentIndex.add(1)));

        findPreviousBtn.disableProperty().bind(Bindings.size(allIndex).lessThanOrEqualTo(0));
        findNextBtn.disableProperty().bind(Bindings.size(allIndex).lessThanOrEqualTo(0));


        setLineNum.setOnAction(event -> {
            if (isShowLineNum.getValue()){
                textEdit.setParagraphGraphicFactory(null);
                isShowLineNum.setValue(false);
            } else {
                textEdit.setParagraphGraphicFactory(LineNumberFactory.get(textEdit));
                isShowLineNum.setValue(true);
            }
        });

        setWarpButton.setOnAction(event -> {
            if (isWarp.getValue()){
                textEdit.setWrapText(false);
                isWarp.setValue(false);
            } else {
                textEdit.setWrapText(true);
                isWarp.setValue(true);
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.isEmpty()){
                allIndex.clear();
                currentIndex.setValue(-1);
                setSelection();
                logger.debug("搜索内容为空，不搜索");
                return;
            }
            String searchContent = newValue.toLowerCase();
            String text = textEdit.getText().toLowerCase();
            int lastVisitIndex = -1;
            if(!currentIndex.getValue().equals(-1)){
                lastVisitIndex = allIndex.get(currentIndex.get());
            }
            allIndex.clear();

            for(int index = -1;(index = text.indexOf(searchContent, index + 1)) != -1; ){
                allIndex.add(index);
            }

            if(allIndex.isEmpty()){
                currentIndex.set(-1);
            } else if(allIndex.contains(lastVisitIndex)){
                currentIndex.set(allIndex.indexOf(lastVisitIndex));
            } else {
                currentIndex.set(0);
            }
            setSelection();
            logger.debug("search result: {}",allIndex.toString());
        });

        showIndexLabel.textProperty().bind(Bindings.format("%d/%d", currentIndex.add(1), Bindings.size(allIndex)));

        
    }


    public void findPrevious(ActionEvent actionEvent) {
        if (allIndex.isEmpty()) return;
        currentIndex.set((currentIndex.get() - 1 + allIndex.size()) % allIndex.size() );
        setSelection();
    }

    public void findNext(ActionEvent actionEvent) {
        if (allIndex.isEmpty()) return;
        currentIndex.set((currentIndex.get() + 1) % allIndex.size());
        setSelection();
    }

    public void setSelection(){
        if(currentIndex.getValue() == -1 || allIndex.size() == 0){
            textEdit.selectRange(0,0);
            logger.debug("无选择");
        }else if(!searchField.getText().isEmpty()){
            textEdit.selectRange(allIndex.get(currentIndex.get()), allIndex.get(currentIndex.get()) + searchField.getText().length());
            textEdit.requestFollowCaret();
        }
    }
}
