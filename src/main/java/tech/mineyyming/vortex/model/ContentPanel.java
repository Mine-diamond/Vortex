package tech.mineyyming.vortex.model;

import tech.mineyyming.vortex.ui.EditorPanel;

public enum ContentPanel {
    EDITORPANEL("EditorPanel.fxml");

    private final String fileName;

    ContentPanel(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName(){
        return this.fileName;
    }
}
