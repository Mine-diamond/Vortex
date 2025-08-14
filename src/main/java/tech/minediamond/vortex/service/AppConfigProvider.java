package tech.minediamond.vortex.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import tech.minediamond.vortex.model.AppConfig;

import java.io.File;
import java.io.IOException;

@Slf4j
@Singleton // 这个 Provider 本身是无状态的，可以标记为 Singleton
public class AppConfigProvider implements Provider<AppConfig> {

    private static final File CONFIG_FILE = new File("config.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // get() 方法是创建 AppConfig 实例的入口
    @Override
    public AppConfig get() {
        log.info("正在加载应用配置");
        try {
            return CONFIG_FILE.exists()
                    ? MAPPER.readValue(CONFIG_FILE, AppConfig.class)
                    : new AppConfig();
        } catch (IOException e) {
            log.error("配置读取失败，使用默认值: {}", e.getMessage());
            return new AppConfig();
        }
    }
}
