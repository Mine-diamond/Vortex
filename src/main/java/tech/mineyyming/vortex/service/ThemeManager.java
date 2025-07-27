package tech.mineyyming.vortex.service;

import javafx.scene.Scene;
import tech.mineyyming.vortex.model.AppConfig;
import tech.mineyyming.vortex.model.AppConfigManager;

import java.util.Objects;

public class ThemeManager {

    private static final AppConfig config = AppConfigManager.getInstance();

    private ThemeManager() {}

    private static class ThemeHolder {
        private static final ThemeManager INSTANCE = new ThemeManager();
    }

    public static ThemeManager getInstance() {
        return ThemeHolder.INSTANCE;
    }

    public void setupToggleTheme(Scene scene) {
        String cssPath = Objects.requireNonNull(getClass().getResource("/tech/mineyyming/vortex/css/fluent-style.css")).toExternalForm();
        String lightCssPath = Objects.requireNonNull(getClass().getResource("/tech/mineyyming/vortex/css/light-theme.css")).toExternalForm();
        String darkCssPath = Objects.requireNonNull(getClass().getResource("/tech/mineyyming/vortex/css/dark-theme.css")).toExternalForm();
        scene.getStylesheets().add(cssPath);
        switch (config.getTheme()) {
            case DARK->{
                scene.getStylesheets().add(darkCssPath);
            }
            case LIGHT->{
                scene.getStylesheets().add(lightCssPath);
            }
        }
        config.themeProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case DARK->{
                    scene.getStylesheets().remove(lightCssPath);
                    scene.getStylesheets().add(darkCssPath);
                }
                case LIGHT->{
                    scene.getStylesheets().remove(darkCssPath);
                    scene.getStylesheets().add(lightCssPath);
                }
            }
        });
    }

    public static void initialize(Scene scene) {
        getInstance().setupToggleTheme(scene);
    }
}
