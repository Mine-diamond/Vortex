package tech.mineyyming.vortex.service;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ShowStageListener implements NativeKeyListener {
    private Stage stage; // 持有对主窗口的引用

    public ShowStageListener(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e){



        if(e.getKeyCode() == NativeKeyEvent.VC_SPACE && ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0)){

            Platform.runLater(() -> {
                if (stage.isShowing()) {
                    //stage.hide();
                    WindowAnimator.hideWindow(stage);
                } else {
                    //stage.show();
                    //stage.toFront();
                    //stage.requestFocus();
                    WindowAnimator.showWindow(stage);
                }
            });

        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {

    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {

    }
}
