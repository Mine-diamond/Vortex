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

package tech.minediamond.vortex.model.appConfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import tech.minediamond.vortex.model.i18n.SupportedLocales;
import tech.minediamond.vortex.model.ui.Theme;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {

    private BooleanProperty showLineNum = new SimpleBooleanProperty(true);
    private BooleanProperty wordWarp = new SimpleBooleanProperty(true);
    private BooleanProperty autoCloseOnFocusLoss = new SimpleBooleanProperty(true);
    private ObjectProperty<Theme> theme = new SimpleObjectProperty<Theme>(Theme.AUTO);
    private BooleanProperty ifCenterOnScreen = new SimpleBooleanProperty(true);
    private ObjectProperty<SupportedLocales> userLocales = new SimpleObjectProperty<>(SupportedLocales.AUTO);
    private BooleanProperty autoStartEnabledProperty = new SimpleBooleanProperty(true);

    public AppConfig(){}

    public BooleanProperty showLineNumProperty() { return showLineNum; }
    public BooleanProperty wordWrapProperty() { return wordWarp; }
    public BooleanProperty autoCloseOnFocusLossProperty() { return autoCloseOnFocusLoss; }
    public ObjectProperty<Theme> themeProperty() { return theme; }
    public BooleanProperty ifCenterOnScreenProperty() { return ifCenterOnScreen; }
    public ObjectProperty<SupportedLocales> userLocalesProperty() { return userLocales; }
    public BooleanProperty autoStartEnabledProperty() { return autoStartEnabledProperty; }

    public void setShowLineNum(boolean showLineNum) {this.showLineNum.set(showLineNum);}
    public boolean getShowLineNum() {return showLineNum.get();}

    public void setWordWarp(boolean wordWarp) {this.wordWarp.set(wordWarp);}
    public boolean getWordWarp() {return wordWarp.get();}

    public void setAutoCloseOnFocusLoss(boolean autoCloseOnFocusLoss) {this.autoCloseOnFocusLoss.set(autoCloseOnFocusLoss);}
    public boolean getAutoCloseOnFocusLoss() {return autoCloseOnFocusLoss.get();}

    //@JsonProperty("theme")
    public void setTheme(Theme theme) {this.theme.set(theme);}
    //@JsonProperty("theme")
    public Theme getTheme() {return theme.get();}

    public void setIfCenterOnScreen(boolean ifCenterOnScreen) {this.ifCenterOnScreen.set(ifCenterOnScreen);}
    public boolean getIfCenterOnScreen() {return ifCenterOnScreen.get();}

    public void setUserLocales(SupportedLocales userLocales) {this.userLocales.set(userLocales);}
    public SupportedLocales getUserLocales() {return userLocales.get();}

    public void setAutoStartEnabledProperty(boolean autoStartEnabledProperty) {this.autoStartEnabledProperty.set(autoStartEnabledProperty);}
    public boolean getAutoStartEnabledProperty() {return autoStartEnabledProperty.get();}
}
