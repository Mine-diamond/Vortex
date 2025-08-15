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
import tech.minediamond.vortex.model.AppConfig;
import tech.minediamond.vortex.service.AppConfigProvider;
import tech.minediamond.vortex.service.ConfigService;
import tech.minediamond.vortex.service.factory.DynamicLineNumberFactoryFactory;
import tech.minediamond.vortex.service.factory.ShowStageListenerFactory;
import tech.minediamond.vortex.service.WindowAnimator;
import tech.minediamond.vortex.service.GetStageService;
import tech.minediamond.vortex.ui.EditorPanel;
import tech.minediamond.vortex.ui.MainWindow;
import tech.minediamond.vortex.ui.SettingPanel;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AppConfig.class).toProvider(AppConfigProvider.class).in(Scopes.SINGLETON);
        bind(ConfigService.class).in(Scopes.SINGLETON);
        bind(GetStageService.class).in(Scopes.SINGLETON);

        bind(MainWindow.class);
        bind(EditorPanel.class);
        bind(SettingPanel.class);

        bind(WindowAnimator.class).in(Scopes.SINGLETON);

        // 安装 AssistedInject 工厂模块
        install(new FactoryModuleBuilder()
                // 告诉 Guice，这个工厂创建出的实例是 ShowStageListener
                //    （如果返回类型就是具体类，这行可以省略，但写上更清晰）
                // .implement(ShowStageListener.class, ShowStageListener.class)

                // 告诉 Guice，这个工厂的实现是基于 ShowStageListenerFactory 接口
                .build(ShowStageListenerFactory.class));

        install(new FactoryModuleBuilder().build(DynamicLineNumberFactoryFactory.class));
    }
}
