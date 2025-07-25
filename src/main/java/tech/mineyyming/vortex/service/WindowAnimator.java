package tech.mineyyming.vortex.service;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 窗口动画管理器 - 改良版单例模式
 *
 * 特点：
 * 1. 单例模式，全局只有一个实例。
 * 2. 支持多窗口管理。
 * 3. 自动延迟初始化，第一次使用时绑定Stage。
 * 4. 提供了带回调的隐藏方法，用于处理“动画后退出”等场景。
 */
public class WindowAnimator {

    private static final Logger logger = LoggerFactory.getLogger(WindowAnimator.class);

    // 使用枚举实现线程安全的单例
    private enum SingletonHolder {
        INSTANCE;
        private final WindowAnimator manager = new WindowAnimator();
    }

    // 用于存储每个窗口的动画上下文
    private final Map<Stage, WindowAnimationContext> windowContexts = new ConcurrentHashMap<>();

    // 私有构造函数
    private WindowAnimator() {}

    /**
     * 获取单例实例
     */
    public static WindowAnimator getInstance() {
        return SingletonHolder.INSTANCE.manager;
    }

    /**
     * 内部类：存储单个窗口的动画上下文信息
     */
    private static class WindowAnimationContext {
        final Timeline showAnimation;
        // 注意：hideAnimation 不再是 final，因为它需要被重新创建以附加不同的回调
        Timeline hideAnimation;
        final Stage stage;

        WindowAnimationContext(Stage stage) {
            this.stage = stage;
            Parent root = stage.getScene().getRoot();

            // --- 创建显示动画 (保持不变) ---
            this.showAnimation = new Timeline(
                    new KeyFrame(javafx.util.Duration.ZERO,
                            new KeyValue(root.opacityProperty(), 0.4),
                            new KeyValue(root.scaleXProperty(), 0.95),
                            new KeyValue(root.scaleYProperty(), 0.95)
                    ),
                    new KeyFrame(javafx.util.Duration.millis(100),
                            new KeyValue(root.opacityProperty(), 1.0),
                            new KeyValue(root.scaleXProperty(), 1.0),
                            new KeyValue(root.scaleYProperty(), 1.0, Interpolator.EASE_OUT)
                    )
            );
        }
    }

    /**
     * 获取或创建指定Stage的动画上下文
     */
    private WindowAnimationContext getOrCreateContext(Stage stage) {
        if (stage == null || stage.getScene() == null || stage.getScene().getRoot() == null) {
            throw new IllegalArgumentException("Stage and its Scene/Root must not be null.");
        }
        return windowContexts.computeIfAbsent(stage, WindowAnimationContext::new);
    }

    // =================================================================
    // 核心公共API (实例方法)
    // =================================================================

    public void playShowAnimation(Stage stage) {
        WindowAnimationContext context = getOrCreateContext(stage);
        Parent root = stage.getScene().getRoot();
        root.setOpacity(1.0);
        root.setScaleX(1.0);
        root.setScaleY(1.0);

        stage.show();
        stage.centerOnScreen();
        context.showAnimation.playFromStart();
        logger.debug("Started show animation for stage: {}", stage.getTitle());
    }

    /**
     * 播放隐藏动画的核心方法，可以附加一个结束回调
     * @param stage 要隐藏的窗口
     * @param onFinishedCallback 动画结束后执行的任务 (可以为 null)
     */
    public void playHideAnimation(Stage stage, Runnable onFinishedCallback) {
        WindowAnimationContext context = getOrCreateContext(stage);
        Parent root = stage.getScene().getRoot();

        // 每次都创建一个新的隐藏动画实例，以附加最新的回调
        context.hideAnimation = new Timeline(
                new KeyFrame(javafx.util.Duration.millis(100),
                        new KeyValue(root.opacityProperty(), 0.0),
                        new KeyValue(root.scaleXProperty(), 0.85),
                        new KeyValue(root.scaleYProperty(), 0.85, Interpolator.EASE_OUT)
                )
        );

        // 设置动画结束后的动作
        context.hideAnimation.setOnFinished(event -> {
            stage.hide(); // 1. 隐藏窗口
            if (onFinishedCallback != null) {
                onFinishedCallback.run(); // 2. 如果有回调，执行它
            }
        });

        context.hideAnimation.playFromStart();
        logger.debug("Started hide animation for stage: {} with callback.", stage.getTitle());
    }

    public void cleanupStage(Stage stage) {
        WindowAnimationContext context = windowContexts.remove(stage);
        if (context != null) {
            context.showAnimation.stop();
            if (context.hideAnimation != null) {
                context.hideAnimation.stop();
            }
            logger.debug("Cleaned up animation context for stage: {}", stage.getTitle());
        }
    }

    // =================================================================
    // 静态便捷方法 (供外部调用)
    // =================================================================

    public static void showWindow(Stage stage) {
        getInstance().playShowAnimation(stage);
    }

    /**
     * 静态方法：隐藏窗口，不带回调
     */
    public static void hideWindow(Stage stage) {
        getInstance().playHideAnimation(stage, null);
    }

    /**
     * 静态方法：隐藏窗口，并在动画结束后执行一个任务
     * @param stage 要隐藏的窗口
     * @param onFinishedCallback 动画结束后执行的任务
     */
    public static void hideWindow(Stage stage, Runnable onFinishedCallback) {
        getInstance().playHideAnimation(stage, onFinishedCallback);
    }

    public static void cleanup(Stage stage) {
        getInstance().cleanupStage(stage);
    }
}
