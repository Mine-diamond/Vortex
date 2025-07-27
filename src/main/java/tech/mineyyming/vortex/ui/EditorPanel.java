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
import javafx.scene.control.ToggleButton;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.mineyyming.vortex.model.AppConfig;
import tech.mineyyming.vortex.model.AppConfigManager;
import tech.mineyyming.vortex.model.DynamicLineNumberFactory;

import java.util.Collections;
import java.util.List;


public class EditorPanel {

    Logger logger = LoggerFactory.getLogger(EditorPanel.class);

    @FXML
    private CodeArea textEdit;
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

    AppConfig config = AppConfigManager.getInstance();

    private ObservableList<Integer> allIndex = FXCollections.observableArrayList();
    private IntegerProperty indexForAllIndex = new SimpleIntegerProperty(-1);
    private StringProperty foundIndexLabelText = new SimpleStringProperty("");
    private boolean lastSearchFieldHasWords = false;
    private String text = "";
    private AddOrDelete addOrDelete = AddOrDelete.ADD;
    private SearchChangeType searchChangeType = SearchChangeType.SEARCH_CONTENT;
    private int startPosition;
    private int endPosition;

    enum AddOrDelete{
        ADD, DELETE
    }

    enum SearchChangeType{
        SEARCH_CONTENT,TEXT
    }

    public void initialize(){

        if(config.wordWrapProperty().getValue()) textEdit.setWrapText(true);
        //if(config.showLineNumProperty().getValue()) textEdit.setParagraphGraphicFactory(LineNumberFactory.get(textEdit));
        if(config.showLineNumProperty().getValue()) textEdit.setParagraphGraphicFactory(DynamicLineNumberFactory.create(textEdit));


        SimpleHoverTooltip.textProperty(setLineNum).bind(Bindings.when(config.showLineNumProperty()).then("显示行号：开").otherwise("显示行号：关"));
        SimpleHoverTooltip.textProperty(setWarpButton).bind(Bindings.when(config.wordWrapProperty()).then("自动换行：开").otherwise("自动换行：关"));
        SimpleHoverTooltip.textProperty(showIndexLabel).bind(Bindings.format("所有匹配数量：%d\n当前位于：%d",Bindings.size(allIndex),indexForAllIndex.add(1)));

        findPreviousBtn.disableProperty().bind(Bindings.size(allIndex).lessThanOrEqualTo(0));
        findNextBtn.disableProperty().bind(Bindings.size(allIndex).lessThanOrEqualTo(0));

        setWarpButton.selectedProperty().bindBidirectional(config.wordWrapProperty());
        textEdit.wrapTextProperty().bindBidirectional(config.wordWrapProperty());

//        setLineNum.setOnAction(event -> {
//            if (config.showLineNumProperty().getValue()){
//                textEdit.setParagraphGraphicFactory(null);
//                config.showLineNumProperty().setValue(false);
//            } else {
//                textEdit.setParagraphGraphicFactory(LineNumberFactory.get(textEdit));
//                config.showLineNumProperty().setValue(true);
//            }
//        });
        setLineNum.selectedProperty().bindBidirectional(config.showLineNumProperty());
        config.showLineNumProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                textEdit.setParagraphGraphicFactory(DynamicLineNumberFactory.create(textEdit));
            } else {
                textEdit.setParagraphGraphicFactory(null);
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchChangeType = SearchChangeType.SEARCH_CONTENT;
            searchWord();
        });

        textEdit.plainTextChanges().subscribe(change -> {
            int position = change.getPosition();
            int insertionEndPosition = change.getInsertionEnd();
            int removalEndPosition = change.getRemovalEnd();

            if(position == insertionEndPosition){
                addOrDelete = AddOrDelete.DELETE;
                startPosition = position;
                endPosition = removalEndPosition;
            } else {
                addOrDelete = AddOrDelete.ADD;
                startPosition = position;
                endPosition = insertionEndPosition;
            }
            searchChangeType = SearchChangeType.TEXT;
            //logger.debug("位置：{}\n插入结束位置：{}\n删除结束位置：{}", position,insertionEndPosition, removalEndPosition);
            //logger.debug("增删：{}\n开始位置：{}\n结束位置：{}",addOrDelete,startPosition,endPosition);

            searchWord();
        });

        showIndexLabel.textProperty().bind(Bindings.format("%d/%d", indexForAllIndex.add(1), Bindings.size(allIndex)));


    }

    public void searchWord(){
        String searchContent = searchField.getText().toLowerCase();
        if(searchContent.isEmpty() && searchChangeType == SearchChangeType.TEXT){

        } else if((searchContent.isEmpty() && searchChangeType == SearchChangeType.SEARCH_CONTENT) || textEdit.getText().isEmpty()){//搜索内容为空会清空结果
            allIndex.clear();
            indexForAllIndex.setValue(-1);
            setSelection(true);
        }else {//搜索内容不为空进行判断
            boolean isRequestFollowCaret = true;
            text = textEdit.getText().toLowerCase();
            if(searchChangeType == SearchChangeType.TEXT){
                isRequestFollowCaret = false;
            }
            int lastVisitIndex = -1;
            if(!indexForAllIndex.getValue().equals(-1)){
                lastVisitIndex = allIndex.get(indexForAllIndex.get());
            }

            if(searchChangeType == SearchChangeType.TEXT && lastVisitIndex != -1){
                if(addOrDelete == AddOrDelete.ADD && endPosition < lastVisitIndex){
                    lastVisitIndex += endPosition-startPosition;
                }else if(addOrDelete == AddOrDelete.DELETE && startPosition < lastVisitIndex){
                    if(endPosition > lastVisitIndex){
                        lastVisitIndex = endPosition;
                    }else {
                        lastVisitIndex -= endPosition-startPosition;
                    }
                }
            }

            allIndex.clear();

            for(int index = -1;(index = text.indexOf(searchContent, index + 1)) != -1; ){
                allIndex.add(index);
            }


            if(allIndex.isEmpty()){
                indexForAllIndex.set(-1);
            } else if(allIndex.contains(lastVisitIndex)){
                indexForAllIndex.set(allIndex.indexOf(lastVisitIndex));
            } else if (!allIndex.contains(lastVisitIndex) && searchChangeType == SearchChangeType.TEXT) {
                lastVisitIndex = findSpecialIndex(allIndex,lastVisitIndex);
            } else {
                indexForAllIndex.set(0);
            }
            setSelection(isRequestFollowCaret);
            logger.debug("search result: {}",allIndex.toString());
        }
    }

    public static int findSpecialIndex(List<Integer> sortedList, int target) {
        // 1. 使用 binarySearch 查找
        int index = Collections.binarySearch(sortedList, target);

        // 2. 分析返回值
        if (index >= 0) {
            return index;
        } else {
            int insertionPoint = -(index + 1);

            if (insertionPoint < sortedList.size()) {
                return insertionPoint;
            } else {
                return sortedList.size() - 1;
            }
        }
    }

    public void findPrevious(ActionEvent actionEvent) {
        if (allIndex.isEmpty()) return;
        indexForAllIndex.set((indexForAllIndex.get() - 1 + allIndex.size()) % allIndex.size() );
        setSelection(true);
    }

    public void findNext(ActionEvent actionEvent) {
        if (allIndex.isEmpty()) return;
        indexForAllIndex.set((indexForAllIndex.get() + 1) % allIndex.size());
        setSelection(true);
    }

    public void setSelection(boolean isRequestFollowCaret){
        if(indexForAllIndex.getValue() == -1 || allIndex.size() == 0){
            textEdit.selectRange(0,0);
            logger.debug("无选择");
        }else if(!searchField.getText().isEmpty()){
            textEdit.selectRange(allIndex.get(indexForAllIndex.get()), allIndex.get(indexForAllIndex.get()) + searchField.getText().length());
            if(isRequestFollowCaret) textEdit.requestFollowCaret();
        }
    }
}
