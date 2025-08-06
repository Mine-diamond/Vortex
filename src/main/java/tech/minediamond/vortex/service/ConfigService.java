package tech.minediamond.vortex.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.AppConfig;

import java.io.File;
import java.io.IOException;

@Slf4j
@Singleton // ConfigService 自身也应该是单例
public class ConfigService {
    private static final File CONFIG_FILE = new File("config.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AppConfig appConfig;

    @Inject
    public ConfigService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public void save() {
        try {
            log.info("正在保存配置...");
            MAPPER.writeValue(CONFIG_FILE, appConfig);
        } catch (IOException e) {
            log.error("写入配置失败: {}", e.getMessage());
        }
    }
}
