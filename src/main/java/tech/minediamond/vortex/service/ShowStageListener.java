package tech.minediamond.vortex.service;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import javafx.application.Platform;
import javafx.stage.Stage;
import tech.minediamond.vortex.model.AppConfig;

public class ShowStageListener implements NativeKeyListener {
    private Stage stage; // 持有对主窗口的引用
    private WindowAnimator windowAnimator;
    private AppConfig appConfig;

    @Inject
    public ShowStageListener(@Assisted Stage stage, WindowAnimator windowAnimator, AppConfig appConfig) {
        this.stage = stage;
        this.windowAnimator = windowAnimator;
        this.appConfig = appConfig;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e){


        if(e.getKeyCode() == NativeKeyEvent.VC_SPACE && ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0)){

            Platform.runLater(() -> {
                if (stage.isShowing()) {
                    windowAnimator.hideWindow(stage);
                } else {
                    windowAnimator.showWindow(stage, appConfig.getIfCenterOnScreen());
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
