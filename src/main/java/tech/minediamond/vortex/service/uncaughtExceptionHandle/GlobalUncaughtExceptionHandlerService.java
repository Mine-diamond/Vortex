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

package tech.minediamond.vortex.service.uncaughtExceptionHandle;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.service.appConfig.AppConfigService;

/**
 * 自定义的全局未捕获异常处理器，这是在任何线程发生未捕获异常时都会执行的逻辑
 * <p>
 * 在[JavaFX Application Thread]，{@link tech.minediamond.vortex.Main#stop()}被调用后该异常处理器依旧会被调用
 */
@Slf4j
public class GlobalUncaughtExceptionHandlerService implements Thread.UncaughtExceptionHandler {
    private final AppConfigService appConfigService;

    @Inject
    public GlobalUncaughtExceptionHandlerService(AppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("捕获到未处理的异常！");
        log.error("异常发生在线程: {}", t.getName());
        log.error("异常类型: {}", e.getClass().getName());
        log.error("异常信息: {}", e.getMessage());
        log.error("堆栈信息:", e);

        // 保存配置
        try {
            appConfigService.save();
        } catch (Exception e1) {
            log.error("保存配置失败: {}", e1.getMessage(), e1);
        }
    }
}
