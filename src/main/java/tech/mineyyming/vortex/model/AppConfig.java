package tech.mineyyming.vortex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {

    private BooleanProperty showLineNum = new SimpleBooleanProperty(true);
    private BooleanProperty wordWarp = new SimpleBooleanProperty(true);
    private BooleanProperty autoCloseOnFocusLoss = new SimpleBooleanProperty(true);

    AppConfig(){}

    public BooleanProperty showLineNumProperty() { return showLineNum; }
    public BooleanProperty wordWrapProperty() { return wordWarp; }
    public BooleanProperty autoCloseOnFocusLossProperty() { return autoCloseOnFocusLoss; }

    public void setshowLineNum(boolean showLineNum) {this.showLineNum.set(showLineNum);}
    public boolean getshowLineNum() {return showLineNum.get();}

    public void setWordWarp(boolean wordWarp) {this.wordWarp.set(wordWarp);}
    public boolean getWordWarp() {return wordWarp.get();}

    public void setAutoCloseOnFocusLoss(boolean autoCloseOnFocusLoss) {this.autoCloseOnFocusLoss.set(autoCloseOnFocusLoss);}
    public boolean getAutoCloseOnFocusLoss() {return autoCloseOnFocusLoss.get();}

    private AppConfig appConfig;

}
