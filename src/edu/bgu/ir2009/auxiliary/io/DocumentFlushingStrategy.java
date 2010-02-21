package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.UnParsedDocument;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 21/02/2010
 * Time: 19:44:39
 */
public class DocumentFlushingStrategy implements FlushingStrategy<List<UnParsedDocument>> {
    private final Configuration config;

    public DocumentFlushingStrategy(Configuration config) {
        this.config = config;
    }

    public String getTempFilePrefix() {
        return "tmp_" + Configuration.SAVED_DOCS_FILE_NAME + "_";
    }

    public void mergePreviousWithNew(List<UnParsedDocument> toFlush, BufferedWriter flushWriter, String previous) throws IOException {
        flushWriter.write(previous);
    }

    public void flushRemainingContent(List<UnParsedDocument> toFlush, BufferedWriter flushWriter) throws IOException {
        for (UnParsedDocument document : toFlush) {
            flushWriter.write(document.serialize());
            flushWriter.write("\n");
        }
        toFlush.clear();
    }

    public String getFinalFileName() {
        return config.getSavedDocsFileName();
    }

    public String getRefFileName() {
        return config.getSavedDocsRefFileName();
    }

    public String getLineRefID(String line) {
        return line.substring(0, line.indexOf('|'));
    }

    public Configuration getConfig() {
        return config;
    }
}
