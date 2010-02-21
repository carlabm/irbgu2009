package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.UnParsedDocument;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 21/02/2010
 * Time: 20:11:37
 */
public class DocumentReadStrategy implements IndexReadStrategy<UnParsedDocument, Object> {
    private final Configuration config;

    public DocumentReadStrategy(Configuration config) {
        this.config = config;
    }

    public String getIndexFileName() {
        return config.getSavedDocsFileName();
    }

    public String getRefFileName() {
        return config.getSavedDocsRefFileName();
    }

    public UnParsedDocument processLine(String line, Object o) {
        return new UnParsedDocument(line);
    }
}
