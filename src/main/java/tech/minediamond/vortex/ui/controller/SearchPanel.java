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

@Singleton
@Slf4j
public class SearchPanel {

    @FXML
    private ScrollPane scrollPane;

    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private String keyword;

    private final SearchService searchService;
    private final I18nService i18n;

    ObjectProperty<SearchStatus> searchStatusProperty = new SimpleObjectProperty<>();
    HBox searchTiphbox = new HBox();
    HBox searchNotFoundTiphbox = new HBox();

    enum SearchStatus {
        SEARCHING, PENDING,NOT_FOUND
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
        scrollPane.contentProperty().bind(searchService.valueProperty());

        Label searchtipLabel = new Label(i18n.t("search.pending.text"));
        searchTiphbox.getChildren().add(searchtipLabel);
        searchTiphbox.setAlignment(Pos.CENTER);

        Label searchNotFoundTipLabel = new Label(i18n.t("search.result.notFound.text"));
        searchNotFoundTiphbox.getChildren().add(searchNotFoundTipLabel);
        searchNotFoundTiphbox.setAlignment(Pos.CENTER);

        searchStatusProperty.addListener((observable, oldValue, newValue) -> {//监控不同的状态展示不同的界面
            switch (newValue) {
                case PENDING -> {
                    scrollPane.contentProperty().unbind();
                    scrollPane.contentProperty().set(searchTiphbox);
                }
                case NOT_FOUND -> {
                    scrollPane.contentProperty().unbind();
                    scrollPane.contentProperty().set(searchNotFoundTiphbox);
                }
                case SEARCHING -> {scrollPane.contentProperty().bind(searchService.valueProperty());}
            }
        });
        searchStatusProperty.set(SearchStatus.PENDING);

        searchService.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(0.0)) {
                searchStatusProperty.set(SearchStatus.NOT_FOUND);
            }
        });
    }

    public void search(String keyword) {
        log.debug("即将搜索");
        searchStatusProperty.set(SearchStatus.SEARCHING);
        this.keyword = keyword;
        debounce.playFromStart();
    }

    public void searchClear(){
        searchStatusProperty.set(SearchStatus.PENDING);
    }

}
