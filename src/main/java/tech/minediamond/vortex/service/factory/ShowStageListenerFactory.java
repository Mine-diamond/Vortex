package tech.minediamond.vortex.service.factory;

import javafx.stage.Stage;
import tech.minediamond.vortex.service.ShowStageListener;

public interface ShowStageListenerFactory {

    ShowStageListener create(Stage stage);

}
