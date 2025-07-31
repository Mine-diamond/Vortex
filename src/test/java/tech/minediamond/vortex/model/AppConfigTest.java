package tech.minediamond.vortex.model;

public class AppConfigTest {
    public static void main(String[] args) {
        AppConfig config = AppConfigManager.getInstance();
        AppConfigManager.save();
    }
}
