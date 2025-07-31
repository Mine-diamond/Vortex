package tech.minediamond.vortex.service;

import javafx.scene.Scene;
import tech.minediamond.vortex.model.AppConfig;
import tech.minediamond.vortex.model.AppConfigManager;

import java.util.Objects;

public class ThemeManager {

    final String cssPath = Objects.requireNonNull(getClass().getResource("/tech/minediamond/vortex/css/fluent-style.css")).toExternalForm();
    final String lightCssPath = Objects.requireNonNull(getClass().getResource("/tech/minediamond/vortex/css/light-theme.css")).toExternalForm();
    final String darkCssPath = Objects.requireNonNull(getClass().getResource("/tech/minediamond/vortex/css/dark-theme.css")).toExternalForm();

    private static final AppConfig config = AppConfigManager.getInstance();

    private ThemeManager() {}

    private static class ThemeHolder {
        private static final ThemeManager INSTANCE = new ThemeManager();
    }

    public static ThemeManager getInstance() {
        return ThemeHolder.INSTANCE;
    }

    public void setupToggleTheme(Scene scene) {
        scene.getStylesheets().add(cssPath);
        switch (config.getTheme()) {
            case DARK -> {
                scene.getStylesheets().add(darkCssPath);
            }
            case LIGHT -> {
                scene.getStylesheets().add(lightCssPath);
            }
        }
        config.themeProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case DARK -> {
                    scene.getStylesheets().remove(lightCssPath);
                    scene.getStylesheets().add(darkCssPath);
                }
                case LIGHT -> {
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
