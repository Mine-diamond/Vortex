package tech.minediamond.vortex.service;

import com.google.inject.Inject;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * 获取stage对象的服务，在 {@link tech.minediamond.vortex.Main#start(Stage)}中初始化并在需要的类中使用{@link #getStage()}获取
 */
@Slf4j
public class GetStageService {

    private Stage stage;

    @Inject
    public GetStageService() {}

    public Stage getStage() {
        if (stage == null) {
            log.error("GetStageService 还没有持有Stage 却被调用了getStage()");
        }
        return stage;
    }

    public void setStage(Stage stage) {
        if (this.stage == null) this.stage = stage;
        log.debug("Stage已被设置：{}", stage);
    }
}
