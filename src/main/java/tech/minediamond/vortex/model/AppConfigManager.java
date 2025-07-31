package tech.minediamond.vortex.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class AppConfigManager {

    private static final File CONFIG_FILE = new File("config.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger logger= LoggerFactory.getLogger(AppConfigManager.class);
    
    private static class AppConfigHolder {
        private static final AppConfig INSTANCE = load();
    }

    public static AppConfig getInstance() {
        return AppConfigHolder.INSTANCE;
    }

    private AppConfigManager() {}

    private static AppConfig load() {
        try {
            return CONFIG_FILE.exists()
                    ? MAPPER.readValue(CONFIG_FILE, AppConfig.class)
                    : new AppConfig();
        } catch (IOException e){
            logger.error("配置读取失败，使用默认值:\n{}",e.getMessage());
            return new AppConfig();
        }
    }

    public static void save() {
        try {
            MAPPER.writeValue(CONFIG_FILE, getInstance());
        } catch (IOException e) {
            logger.error("写入配置失败:\n{}",e.getMessage());
        }
    }
}
