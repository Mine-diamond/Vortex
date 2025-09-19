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

package tech.minediamond.vortex.ui.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.service.i18n.I18nService;
import tech.minediamond.vortex.service.search.SearchService;

/**
 * 搜索面板控制器类，负责处理搜索界面的逻辑和状态管理
 */
@Singleton
@Slf4j
public class SearchPanel {

    @FXML
    private ScrollPane scrollPane;

    // 防抖机制：延迟300毫秒执行搜索，避免频繁触发搜索请求
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private String keyword;

    private final SearchService searchService;
    private final I18nService i18n;

    // 搜索状态属性，用于监控和响应搜索状态变化
    ObjectProperty<SearchStatus> searchStatusProperty = new SimpleObjectProperty<>();
    // 搜索提示和未找到结果的提示容器
    HBox searchTiphbox = new HBox();
    HBox searchNotFoundTiphbox = new HBox();
    HBox serviceErrorTiphbox = new HBox();

    /**
     * 搜索状态枚举，定义搜索过程中的不同状态
     */
    enum SearchStatus {
        SEARCHING, SEARCHED, PENDING, NOT_FOUND, SERVICE_ERROR
    }

    @Inject
    public SearchPanel(SearchService searchService, I18nService i18n) {
        this.searchService = searchService;
        this.i18n = i18n;

        debounce.setOnFinished(event -> {
            log.info("开始搜索");
            searchService.search(keyword);
        });

    }

    public void initialize() {
        // 等待搜索提示
        Label searchtipLabel = new Label(i18n.t("search.pending.text"));
        searchTiphbox.getChildren().add(searchtipLabel);
        searchTiphbox.setAlignment(Pos.CENTER);

        // 未找到结果提示
        Label searchNotFoundTipLabel = new Label(i18n.t("search.result.notFound.text"));
        searchNotFoundTiphbox.getChildren().add(searchNotFoundTipLabel);
        searchNotFoundTiphbox.setAlignment(Pos.CENTER);

        Label serviceErrorTipLabel = new Label("abc");
        serviceErrorTiphbox.getChildren().add(serviceErrorTipLabel);
        serviceErrorTiphbox.setAlignment(Pos.CENTER);

        // 监听搜索状态变化，根据状态更新界面显示
        searchStatusProperty.addListener((observable, oldValue, newValue) -> {//监控不同的状态展示不同的界面
            switch (newValue) {
                case PENDING -> {
                    scrollPane.contentProperty().set(searchTiphbox);
                }
                case NOT_FOUND -> {
                    scrollPane.contentProperty().set(searchNotFoundTiphbox);
                }
                case SERVICE_ERROR -> {
                    scrollPane.contentProperty().set(serviceErrorTiphbox);
                }
                case SEARCHING -> {}//显示处于搜索状态时显示之前的画面，考虑到Everything引擎搜索速度极快，不显示专门的搜索中页面
                case SEARCHED -> {scrollPane.contentProperty().set(searchService.valueProperty().get());}
            }
        });
        searchStatusProperty.set(SearchStatus.PENDING);

        // 监听搜索进度变化，根据进度更新搜索状态，0代表未找到结果，1代表搜索完成
        searchService.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(0.0)) {
                searchStatusProperty.set(SearchStatus.NOT_FOUND);
            } else if(newValue.equals(1.0)) {
                searchStatusProperty.set(SearchStatus.SEARCHED);
            } else if (newValue.equals(0.5)) {
                searchStatusProperty.set(SearchStatus.SERVICE_ERROR);
            }
        });
    }

    /**
     * 执行搜索方法
     * @param keyword 搜索关键词
     */
    public void search(String keyword) {
        log.debug("即将搜索");
        searchStatusProperty.set(SearchStatus.SEARCHING);
        this.keyword = keyword;
        debounce.playFromStart();
    }

    /**
     * 清除搜索状态，重置为等待搜索状态
     */
    public void searchClear(){
        searchStatusProperty.set(SearchStatus.PENDING);
    }

}
