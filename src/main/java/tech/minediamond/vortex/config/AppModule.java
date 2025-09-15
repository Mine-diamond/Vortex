/*
 * Vortex
 * Copyright (C) 2025 Mine-diamond
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package tech.minediamond.vortex.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import tech.minediamond.vortex.model.appConfig.AppConfig;
import tech.minediamond.vortex.service.appConfig.AppConfigProvider;
import tech.minediamond.vortex.service.appConfig.AppConfigService;
import tech.minediamond.vortex.service.autoStart.MockAutoStartService;
import tech.minediamond.vortex.service.autoStart.WindowsAutoStartService;
import tech.minediamond.vortex.service.ui.ShowStageListenerFactory;
import tech.minediamond.vortex.service.autoStart.IAutoStartService;
import tech.minediamond.vortex.service.i18n.I18nService;
import tech.minediamond.vortex.service.search.EverythingService;
import tech.minediamond.vortex.service.ui.AutoOperateService;
import tech.minediamond.vortex.service.ui.StageProvider;
import tech.minediamond.vortex.service.ui.TrayMenuService;
import tech.minediamond.vortex.service.ui.WindowAnimator;
import tech.minediamond.vortex.service.uncaughtExceptionHandle.GlobalUncaughtExceptionHandlerService;
import tech.minediamond.vortex.ui.controller.EditorPanel;
import tech.minediamond.vortex.ui.controller.MainWindow;
import tech.minediamond.vortex.ui.controller.SettingPanel;
import tech.minediamond.vortex.util.EnvironmentDetector;
import tech.minediamond.vortex.util.OpenResourceService;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AppConfig.class).toProvider(AppConfigProvider.class).in(Scopes.SINGLETON);
        bind(AppConfigProvider.class).asEagerSingleton();
        bind(AppConfigService.class).in(Scopes.SINGLETON);
        bind(StageProvider.class).in(Scopes.SINGLETON);
        bind(AutoOperateService.class).in(Scopes.SINGLETON);
        bind(I18nService.class);
        bind(EverythingService.class).asEagerSingleton();
        bind(OpenResourceService.class);
        bind(GlobalUncaughtExceptionHandlerService.class).in(Scopes.SINGLETON);


        bind(MainWindow.class);
        bind(EditorPanel.class);
        bind(SettingPanel.class);
        bind(TrayMenuService.class);

        bind(WindowAnimator.class).in(Scopes.SINGLETON);

        if(EnvironmentDetector.isProduction()){//根据程序是在开发环境还是打包环境选择不同的类
            bind(IAutoStartService.class).to(WindowsAutoStartService.class).in(Scopes.SINGLETON);
        } else {
            bind(IAutoStartService.class).to(MockAutoStartService.class).in(Scopes.SINGLETON);
        }

        // 安装 AssistedInject 工厂模块
        install(new FactoryModuleBuilder()
                // 告诉 Guice，这个工厂创建出的实例是 ShowStageListener
                //    （如果返回类型就是具体类，这行可以省略，但写上更清晰）
                // .implement(ShowStageListener.class, ShowStageListener.class)

                // 告诉 Guice，这个工厂的实现是基于 ShowStageListenerFactory 接口
                .build(ShowStageListenerFactory.class));


    }
}
