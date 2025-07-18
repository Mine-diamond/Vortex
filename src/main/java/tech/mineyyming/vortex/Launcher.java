package tech.mineyyming.vortex;

/**
 * 这是一个包装启动类，用于解决 jpackage 和 fat-jar 的模块化冲突问题。
 * 这个类本身不是 JavaFX 应用，它的 main 方法只是简单地调用真正的 Main 类的 main 方法。
 */
public class Launcher {
    public static void main(String[] args) {
        // 调用你真正的 Main 类的 main 方法
        Main.main(args);
    }
}