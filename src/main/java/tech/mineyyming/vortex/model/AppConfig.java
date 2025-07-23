package tech.mineyyming.vortex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {

    private BooleanProperty showLineNum = new SimpleBooleanProperty(true);
    private BooleanProperty wordWarp = new SimpleBooleanProperty(true);
    private Boolean alwaysOnTop = true;

    AppConfig(){}

    public BooleanProperty showLineNumProperty() { return showLineNum; }
    public BooleanProperty wordWrapProperty() { return wordWarp; }
    public Boolean alwaysOnTop() { return alwaysOnTop; }

    public void setshowLineNum(boolean showLineNum) {this.showLineNum.set(showLineNum);}
    public boolean getshowLineNum() {return showLineNum.get();}

    public void setWordWarp(boolean wordWarp) {this.wordWarp.set(wordWarp);}
    public boolean getWordWarp() {return wordWarp.get();}

    public void setAlwaysOnTop(boolean alwaysOnTop) {this.alwaysOnTop = alwaysOnTop;}
    public boolean getAlwaysOnTop() {return alwaysOnTop;}

    private AppConfig appConfig;

}
