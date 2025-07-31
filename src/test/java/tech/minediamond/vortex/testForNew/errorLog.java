package tech.minediamond.vortex.testForNew;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class errorLog {

    static Logger logger = LoggerFactory.getLogger(errorLog.class);

    public static void main(String[] args) {
        try {
            int a = 1/0;
        } catch (Exception e) {
            logger.error("as",e);
        }
    }
}
