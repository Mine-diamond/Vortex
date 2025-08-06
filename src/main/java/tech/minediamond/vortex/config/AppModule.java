package tech.minediamond.vortex.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import tech.minediamond.vortex.model.AppConfig;
import tech.minediamond.vortex.service.AppConfigProvider;
import tech.minediamond.vortex.service.ConfigService;
import tech.minediamond.vortex.ui.EditorPanel;
import tech.minediamond.vortex.ui.MainWindow;
import tech.minediamond.vortex.ui.SettingPanel;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AppConfig.class).toProvider(AppConfigProvider.class).in(Scopes.SINGLETON);
        bind(ConfigService.class).in(Scopes.SINGLETON);

        bind(MainWindow.class);
        bind(EditorPanel.class);
        bind(SettingPanel.class);
    }
}
