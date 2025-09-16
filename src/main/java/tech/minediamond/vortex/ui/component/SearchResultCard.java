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

package tech.minediamond.vortex.ui.component;

import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;
import lombok.Getter;
import tech.minediamond.vortex.model.search.EverythingResult;

public class SearchResultCard extends Control {

    @Getter
    private final EverythingResult result;

    public SearchResultCard(EverythingResult result) {
        this.result = result;
    }

    @Override
    protected Skin createDefaultSkin() {
        return new Skin(this);
    }



    private static final class Skin extends SkinBase<SearchResultCard> {

        VBox vbox = new VBox();

        /**
         * Constructor for all SkinBase instances.
         *
         * @param control The control for which this Skin should attach to.
         */
        protected Skin(SearchResultCard control) {
            super(control);

            Label fileNameLabel = new Label(control.result.getFileName());
            Label filePathLabel = new Label(control.result.getFullPath());

            vbox.getChildren().addAll(fileNameLabel, filePathLabel);
            getChildren().add(vbox);
        }
    }

}
