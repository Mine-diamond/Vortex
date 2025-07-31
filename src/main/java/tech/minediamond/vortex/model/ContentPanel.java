package tech.minediamond.vortex.model;

public enum ContentPanel {
    EDITORPANEL("editorPanel.fxml"),
    SETTINGPANEL("settingPanel.fxml");

    private final String fileName;

    ContentPanel(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName(){
        return this.fileName;
    }
}
