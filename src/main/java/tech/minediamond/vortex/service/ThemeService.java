package tech.minediamond.vortex.service;

import com.google.inject.Inject;
import javafx.scene.Scene;
import tech.minediamond.vortex.model.AppConfig;

import java.util.Objects;

public class ThemeService {

    final String cssPath = Objects.requireNonNull(getClass().getResource("/tech/minediamond/vortex/css/fluent-style.css")).toExternalForm();
    final String lightCssPath = Objects.requireNonNull(getClass().getResource("/tech/minediamond/vortex/css/light-theme.css")).toExternalForm();
    final String darkCssPath = Objects.requireNonNull(getClass().getResource("/tech/minediamond/vortex/css/dark-theme.css")).toExternalForm();

    private static AppConfig config;

    @Inject
    public ThemeService(AppConfig config) {
        this.config = config;
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

    public void initialize(Scene scene) {
        setupToggleTheme(scene);
    }
}
