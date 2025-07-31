package tech.minediamond.vortex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {

    private BooleanProperty showLineNum = new SimpleBooleanProperty(true);
    private BooleanProperty wordWarp = new SimpleBooleanProperty(true);
    private BooleanProperty autoCloseOnFocusLoss = new SimpleBooleanProperty(true);
    private ObjectProperty<Theme> theme = new SimpleObjectProperty<Theme>(Theme.LIGHT);

    AppConfig(){}

    public BooleanProperty showLineNumProperty() { return showLineNum; }
    public BooleanProperty wordWrapProperty() { return wordWarp; }
    public BooleanProperty autoCloseOnFocusLossProperty() { return autoCloseOnFocusLoss; }
    public ObjectProperty<Theme> themeProperty() { return theme; }

    public void setshowLineNum(boolean showLineNum) {this.showLineNum.set(showLineNum);}
    public boolean getshowLineNum() {return showLineNum.get();}

    public void setWordWarp(boolean wordWarp) {this.wordWarp.set(wordWarp);}
    public boolean getWordWarp() {return wordWarp.get();}

    public void setAutoCloseOnFocusLoss(boolean autoCloseOnFocusLoss) {this.autoCloseOnFocusLoss.set(autoCloseOnFocusLoss);}
    public boolean getAutoCloseOnFocusLoss() {return autoCloseOnFocusLoss.get();}

    //@JsonProperty("theme")
    public void setTheme(Theme theme) {this.theme.set(theme);}
    //@JsonProperty("theme")
    public Theme getTheme() {return theme.get();}
}
