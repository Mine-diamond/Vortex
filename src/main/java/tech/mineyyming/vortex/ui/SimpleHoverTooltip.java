package tech.mineyyming.vortex.ui;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

/**
 * 一个管理器类，用于为任何 Node 安装和管理一个悬浮提示 Popup。
 * 新版本使用 JavaFX 原生的 Popup 实现，替代了 ControlsFX 的 PopOver。
 * 支持通过 FXML 附加属性进行声明式使用。
 */
public class SimpleHoverTooltip {

    private static final Object HOVER_TOOLTIP_KEY = new Object();
    private static final double GAP_FROM_OWNER = 5.0;

    private final Node owner;
    private final Popup popup;
    private final Label textLabel;
    private final StackPane contentPane;
    private final EventHandler<MouseEvent> enterHandler;
    private final EventHandler<MouseEvent> exitHandler;

    private SimpleHoverTooltip(Node owner, String tooltipText) {
        this.owner = owner;
        this.popup = new Popup();
        this.textLabel = new Label(tooltipText);
        this.textLabel.setTextFill(Color.WHITE);

        this.contentPane = new StackPane(textLabel);
        contentPane.setPadding(new Insets(1, 2, 1, 2));
        contentPane.setStyle("-fx-background-color: #424242; -fx-background-radius: 2;");

        this.popup.getContent().add(contentPane);
        this.popup.setAutoFix(false); // 与原 PopOver 行为保持一致
        this.popup.setAutoHide(true);
        this.popup.setHideOnEscape(true);

        // 当 Popup 第一次显示时，其尺寸才被计算出来。
        // 我们利用这个时机来精确定位它。
        this.popup.setOnShown(event -> {
            Bounds ownerBounds = this.owner.localToScreen(this.owner.getBoundsInLocal());
            // 计算目标位置，使 tooltip 水平居中于 owner 之上
            double targetX = ownerBounds.getCenterX() - popup.getWidth() / 2;
            double targetY = ownerBounds.getMinY() - popup.getHeight() - GAP_FROM_OWNER;
            this.popup.setX(targetX);
            this.popup.setY(targetY);
        });

        this.enterHandler = event -> {
            if (!this.popup.isShowing()) {
                // 先在任意位置显示，以触发 onShown 事件来计算真实位置
                this.popup.show(this.owner, 0, 0);
            }
        };

        this.exitHandler = event -> this.popup.hide();

        this.owner.addEventHandler(MouseEvent.MOUSE_ENTERED, this.enterHandler);
        this.owner.addEventHandler(MouseEvent.MOUSE_EXITED, this.exitHandler);
    }

    public void setText(String newText) {
        this.textLabel.setText(newText);
    }

    public void uninstall() {
        this.popup.hide();
        this.owner.removeEventHandler(MouseEvent.MOUSE_ENTERED, this.enterHandler);
        this.owner.removeEventHandler(MouseEvent.MOUSE_EXITED, this.exitHandler);
        this.owner.getProperties().remove(HOVER_TOOLTIP_KEY);
    }

    // --- 核心逻辑：安装或更新实例 ---
    private static SimpleHoverTooltip installOrUpdate(Node owner, String text) {
        SimpleHoverTooltip instance = (SimpleHoverTooltip) owner.getProperties().get(HOVER_TOOLTIP_KEY);
        if (instance != null) {
            instance.setText(text);
            return instance;
        } else {
            SimpleHoverTooltip newInstance = new SimpleHoverTooltip(owner, text);
            owner.getProperties().put(HOVER_TOOLTIP_KEY, newInstance);
            return newInstance;
        }
    }

    private static void uninstall(Node owner) {
        SimpleHoverTooltip instance = (SimpleHoverTooltip) owner.getProperties().get(HOVER_TOOLTIP_KEY);
        if (instance != null) {
            instance.uninstall();
        }
    }

    // ========================================================================
    // == FXML 附加属性支持 (THE MAGIC HAPPENS HERE) ==
    // ========================================================================

    /**
     * FXML 附加属性的 setter 方法。
     * 当 FXML 解析器遇到 <Node SimpleHoverTooltip.text="..."/> 时，会调用此方法。
     * @param node  应用此属性的节点 (例如 Button)
     * @param text  来自 FXML 的文本值
     */
    public static void setText(Node node, String text) {
        if (text == null || text.trim().isEmpty()) {
            uninstall(node);
        } else {
            installOrUpdate(node, text);
        }
    }

    /**
     * FXML 附加属性的 getter 方法。
     * 主要用于 Scene Builder 等工具，或者在代码中读取值。
     * @param node 目标节点
     * @return 附加在该节点上的 tooltip 文本，如果没有则返回 null
     */
    public static String getText(Node node) {
        SimpleHoverTooltip instance = (SimpleHoverTooltip) node.getProperties().get(HOVER_TOOLTIP_KEY);
        return (instance != null) ? instance.textLabel.getText() : null;
    }
}
