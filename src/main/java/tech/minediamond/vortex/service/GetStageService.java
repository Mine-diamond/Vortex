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

package tech.minediamond.vortex.service;

import com.google.inject.Inject;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * 获取stage对象的服务，在 {@link tech.minediamond.vortex.Main#start(Stage)}中初始化并在需要的类中使用{@link #getStage()}获取
 */
@Slf4j
public class GetStageService {

    private Stage stage;

    @Inject
    public GetStageService() {}

    public Stage getStage() {
        if (stage == null) {
            log.error("GetStageService 还没有持有Stage 却被调用了getStage()");
        }
        return stage;
    }

    public void setStage(Stage stage) {
        if (this.stage == null) this.stage = stage;
        log.debug("Stage已被设置：{}", stage);
    }
}
