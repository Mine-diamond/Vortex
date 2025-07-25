package tech.mineyyming.vortex.service;

import javafx.beans.property.BooleanProperty;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 一个包含自定义绑定逻辑的工具类。
 */
public final class BindingUtils {

    /**
     * 私有构造函数，防止这个工具类被实例化。
     * 工具类应该只包含静态方法。
     */
    private BindingUtils() {}

    /**
     * 将两个布尔属性进行双向“反向”绑定。
     * 当一个属性变为 true 时，另一个将变为 false，反之亦然。
     *
     * @param prop1 第一个布尔属性
     * @param prop2 第二个布尔属性
     */
    public static void bindBidirectionalInverse(BooleanProperty prop1, BooleanProperty prop2) {
        // 使用 AtomicBoolean 作为“信号旗”。
        // 为什么用 AtomicBoolean 而不是普通的 boolean？
        // 1. 因为 Lambda 表达式中引用的外部局部变量必须是 final 或 "effectively final" 的。
        //    我们不能直接在 Lambda 中修改一个普通的 boolean 变量。
        // 2. AtomicBoolean 是一个对象，它的引用是 final 的，但它内部的值可以被修改，完美绕过限制。
        // 3. 它也是线程安全的，虽然在这个场景下不是必须，但这是个好习惯。
        final AtomicBoolean updating = new AtomicBoolean(false);

        // 为 prop1 添加监听器
        prop1.addListener((observable, oldValue, newValue) -> {
            // 如果 updating 标志为 true，说明是我们的代码正在更新，直接返回以防止循环。
            if (updating.get()) {
                return;
            }

            // 使用 try-finally 结构是一个非常健壮的模式。
            // 它可以确保无论 set() 操作是否成功，updating 标志最终都会被重置为 false。
            try {
                // 1. 升起信号旗，表示“我正要更新，别捣乱！”
                updating.set(true);
                // 2. 执行反向更新
                prop2.set(!newValue);
            } finally {
                // 3. 操作完成，放下信号旗
                updating.set(false);
            }
        });

        // 为 prop2 添加功能完全相同的监听器
        prop2.addListener((observable, oldValue, newValue) -> {
            if (updating.get()) {
                return;
            }
            try {
                updating.set(true);
                prop1.set(!newValue);
            } finally {
                updating.set(false);
            }
        });

        // 初始同步：让 prop2 的状态与 prop1 的初始状态相反
        prop2.set(!prop1.get());
    }
}

