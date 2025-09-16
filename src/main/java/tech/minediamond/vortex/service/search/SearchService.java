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

package tech.minediamond.vortex.service.search;

import com.google.inject.Inject;
import com.google.inject.Injector;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.fileData.FileData;
import tech.minediamond.vortex.model.search.SearchMode;
import tech.minediamond.vortex.service.i18n.I18nService;
import tech.minediamond.vortex.ui.component.ComponentList;
import tech.minediamond.vortex.ui.component.SearchResultCard;
import tech.minediamond.vortex.ui.component.SearchResultCardFactory;
import tech.minediamond.vortex.util.ClipboardUtil;
import tech.minediamond.vortex.util.OpenResourceUtil;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class SearchService extends Service<ComponentList> {

    private final String NAME = "Search Thread";

    private final EverythingService everythingService;
    private final I18nService i18n;
    private final Injector injector;
    private final StringProperty keyword = new SimpleStringProperty();

    private final ThreadFactory searchThreadFactory;
    private final ExecutorService executor;

    @Inject
    public SearchService(EverythingService everythingService, I18nService i18n, Injector injector) {
        this.everythingService = everythingService;
        this.i18n = i18n;
        this.injector = injector;

        searchThreadFactory = r -> {
            Thread t = new Thread(r, NAME);
            t.setDaemon(true);
            return t;
        };
        executor = Executors.newSingleThreadExecutor(searchThreadFactory);
        setExecutor(executor);

    }

    public String getKeyword() {
        return keyword.get();
    }

    public void search(String keyword) {
        this.keyword.set(keyword);
        restart();
    }

    @Override
    protected Task<ComponentList> createTask() {
        return new Task<ComponentList>() {

            @Override
            protected ComponentList call() throws Exception {
                List<FileData> results = everythingService.QueryBuilder()
                        .mode(SearchMode.ALL)
                        .searchFor(keyword.get())
                        .query();

                ComponentList componentList = new ComponentList();

                if (results.isEmpty()) {
                    updateProgress(0, 1);
                    return null;
                }

                for (FileData result : results) {
                    SearchResultCard card = injector.getInstance(SearchResultCardFactory.class).create(result);
                    card.setOnOpen(OpenResourceUtil::OpenFile);
                    card.setOnRevealInFolder(OpenResourceUtil::OpenFileInFolder);
                    card.setOnCopy(fileData -> ClipboardUtil.copyToClipboard(fileData.getFullPath()));
                    card.setOnOpenPathInTerminal(OpenResourceUtil::OpenPathInTerminal);
                    componentList.addNode(card);
                }

                log.info("搜索成功");
                return componentList;
            }
        };
    }

}
