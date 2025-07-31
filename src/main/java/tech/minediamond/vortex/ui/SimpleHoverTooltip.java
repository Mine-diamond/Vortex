package tech.minediamond.vortex.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;

/**
 * 一个管理器类，用于为任何 Node 安装和管理一个悬浮提示 Popup。
 * 新版本使用 JavaFX 原生的 Popup 实现，并完全支持 JavaFX 属性绑定。
 * 支持通过 FXML 附加属性进行声明式使用。
 *
 * @version 2.1 - Fixed positioning issue on text update by listening to size changes.
 */
public class SimpleHoverTooltip {

    // --- 内部常量 ---
    private static final Object TOOLTIP_INSTANCE_KEY = new Object();
    private static final Object TOOLTIP_PROPERTY_KEY = new Object();
    private static final double GAP_FROM_OWNER = 5.0;

    // --- 实例字段 ---
    private final Node owner;
    private final Popup popup;
    private final Label textLabel;

    private final EventHandler<MouseEvent> enterHandler;
    private final EventHandler<MouseEvent> exitHandler;

    private SimpleHoverTooltip(Node owner) {
        this.owner = owner;
        this.popup = new Popup();
        this.textLabel = new Label();

        StackPane contentPane = new StackPane(textLabel);
        contentPane.setPadding(new Insets(1, 2, 1, 2));
        contentPane.getStyleClass().add("simple-hover-tooltip");

        this.popup.getContent().add(contentPane);
        this.popup.setAutoFix(false);
        this.popup.setAutoHide(true);
        this.popup.setHideOnEscape(true);


        this.popup.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (popup.isShowing()) positionPopup();
        });
        this.popup.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (popup.isShowing()) positionPopup();
        });

        this.enterHandler = event -> {
            if (!this.popup.isShowing()) {

                this.popup.show(this.owner, 0, 0);
            }
        };
        this.exitHandler = event -> this.popup.hide();

        this.owner.addEventHandler(MouseEvent.MOUSE_ENTERED, this.enterHandler);
        this.owner.addEventHandler(MouseEvent.MOUSE_EXITED, this.exitHandler);
    }

    /**
     * 更新 Tooltip 显示的文本。
     */
    private void updateText(String newText) {
        this.textLabel.setText(newText);
        // ✅ [代码简化] 不再需要在这里手动调用 positionPopup()。
        // 文本更新导致的尺寸变化会自动被 width/height 监听器捕获。
    }

    /**
     * 重新计算并设置 Popup 的位置。
     * 这个方法现在只在 popup 尺寸确定或变化后被调用，因此是准确的。
     */
    private void positionPopup() {
        if (!owner.getScene().getWindow().isShowing()) {
            // 防止在 owner 窗口不可见时执行定位
            return;
        }
        Bounds ownerBounds = this.owner.localToScreen(this.owner.getBoundsInLocal());
        // 计算目标位置，使 tooltip 水平居中于 owner 之上
        double targetX = ownerBounds.getCenterX() - popup.getWidth() / 2;
        double targetY = ownerBounds.getMinY() - popup.getHeight() - GAP_FROM_OWNER;
        this.popup.setX(targetX);
        this.popup.setY(targetY);
    }

    /**
     * 卸载 Tooltip，移除事件处理器并清理资源。
     */
    private void uninstall() {
        this.popup.hide();
        this.owner.removeEventHandler(MouseEvent.MOUSE_ENTERED, this.enterHandler);
        this.owner.removeEventHandler(MouseEvent.MOUSE_EXITED, this.exitHandler);
        this.owner.getProperties().remove(TOOLTIP_INSTANCE_KEY);
    }


    public static StringProperty textProperty(Node node) {
        StringProperty textProperty = (StringProperty) node.getProperties().get(TOOLTIP_PROPERTY_KEY);
        if (textProperty == null) {
            textProperty = new SimpleStringProperty(null) {
                @Override
                protected void invalidated() {
                    handleTextChange(node, get());
                }
            };
            node.getProperties().put(TOOLTIP_PROPERTY_KEY, textProperty);
        }
        return textProperty;
    }

    public static void setText(Node node, String text) {
        textProperty(node).set(text);
    }

    public static String getText(Node node) {
        return textProperty(node).get();
    }

    private static void handleTextChange(Node owner, String newText) {
        SimpleHoverTooltip instance = (SimpleHoverTooltip) owner.getProperties().get(TOOLTIP_INSTANCE_KEY);
        boolean textIsNullOrEmpty = (newText == null || newText.trim().isEmpty());

        if (textIsNullOrEmpty) {
            if (instance != null) {
                instance.uninstall();
            }
        } else {
            if (instance != null) {
                instance.updateText(newText);
            } else {
                SimpleHoverTooltip newInstance = new SimpleHoverTooltip(owner);
                newInstance.updateText(newText);
                owner.getProperties().put(TOOLTIP_INSTANCE_KEY, newInstance);
            }
        }
    }
}
