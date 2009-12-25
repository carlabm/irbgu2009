package edu.bgu.ir2009;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 25/12/2009
 * Time: 16:01:15
 */
public class Configuration {
    private final Properties config;

    public Configuration(String configFileName) throws IOException {
        this.config = new Properties();
        config.load(new FileInputStream(configFileName));
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
}
