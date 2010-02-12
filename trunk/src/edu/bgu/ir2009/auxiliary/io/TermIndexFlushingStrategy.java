package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.TermData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 12/02/2010
 * Time: 20:04:30
 */
public class TermIndexFlushingStrategy implements FlushingStrategy<Map<String, TermData>> {
    private final Configuration config;

    public TermIndexFlushingStrategy(Configuration config) {
        this.config = config;
    }

    public String getTempFilePrefix() {
        return "tmp_index_";
    }

    public void mergePreviousWithNew(Map<String, TermData> toFlush, BufferedWriter flushWriter, String line) throws IOException {

    }

    public void flushRemainingContent(Map<String, TermData> toFlush, BufferedWriter flushWriter) throws IOException {

    }

    public String getFinalFileName() {
        return config.getIndexFileName();
    }

    public String getRefFileName() {
        return config.getIndexReferenceFileName();
    }

    public String getLineRefID(String line) {
        return line.substring(0, line.indexOf(':'));
    }
}
