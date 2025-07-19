package tech.mineyyming.vortex.ui;

//import javafx.fxml.FXML;
//import javafx.scene.Node;
//import javafx.scene.layout.BorderPane;
//
//enum MaxSizeNode{
//    NONE,EDITORPANEL
//}
//
public class DefaultView {
//
//    @FXML
//    private Node editorPanel; // 注入右侧视图
//    @FXML
//    private EditorPanel editorPanelController; // 注入右侧视图的控制器
//    @FXML
//    private BorderPane defaultViewBorder;
//
//    MaxSizeNode maxSizeNode = MaxSizeNode.NONE;
//
//    public void initialize(){
//        // 关键一步：将具体的放大/缩小逻辑“注入”到子控制器中
//        editorPanelController.setOnToggleSizeRequest(() -> {
//            // 这个 lambda 表达式就是当 EditorPanel 按钮被点击时要执行的代码
//            System.out.println("DefaultView: Received toggle size request. Executing...");
//            toggleEditorPanelSize();
//        });
//    }
//
//
//    private void toggleEditorPanelSize() {
//        switch(maxSizeNode){
//            case NONE -> {
//                maxSizeNode = MaxSizeNode.EDITORPANEL;
//                defaultViewBorder.setRight(null);
//                defaultViewBorder.setCenter(editorPanel);
//                editorPanelController.updateToggleButtonText(true);
//                System.out.println("DefaultView: Setting editor panel size to " + maxSizeNode);
//            }
//            case EDITORPANEL -> {
//                maxSizeNode = MaxSizeNode.NONE;
//                defaultViewBorder.setCenter(null);
//                defaultViewBorder.setRight(editorPanel);
//                editorPanelController.updateToggleButtonText(false);
//                System.out.println("DefaultView: Setting editor panel size to " + maxSizeNode);
//            }
//        }
//    }
//
}
