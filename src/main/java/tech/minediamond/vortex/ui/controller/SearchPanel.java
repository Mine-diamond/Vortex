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
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.service.search.SearchService;

@Singleton
@Slf4j
public class SearchPanel {

    @FXML
    private ScrollPane scrollPane;

    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private String keyword;

    private final SearchService searchService;

    @Inject
    public SearchPanel(SearchService searchService) {
        this.searchService = searchService;

        debounce.setOnFinished(event -> {
            searchService.search(keyword);
        });
    }

    public void initialize() {
        scrollPane.contentProperty().bind(searchService.valueProperty());
    }

    public void search(String keyword) {
        log.info("Search started");
        this.keyword = keyword;
        debounce.playFromStart();
    }

}
