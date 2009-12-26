package edu.bgu.ir2009;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 25/12/2009
 * Time: 16:01:15
 */
public class Configuration {
    private static final Logger logger = Logger.getLogger(Configuration.class);
    private final Properties config;

    public Configuration(String configFileName) throws IOException {
        config = new Properties();
        config.load(new FileInputStream(configFileName));
        String message = "Loaded configurations file successfully: " + configFileName + "\n" +
                config;
        logger.debug(message);
    }

    public boolean useStemmer() {
        return Boolean.parseBoolean(config.getProperty("use_stemmer"));
    }

    public String getDocumentsDir() {
        return config.getProperty("doc_dir");
    }

    public String getStopWordsFileName() {
        return config.getProperty("stop_words");
    }

    public String getParsedDocsDir() {
        return config.getProperty("saved_docs_dir");
    }
}
