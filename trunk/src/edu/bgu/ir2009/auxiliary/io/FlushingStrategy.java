package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: Jan 28, 2010
 * Time: 7:06:42 PM
 */
public interface FlushingStrategy<T> {
    String getTempFilePrefix();

    void mergePreviousWithNew(T toFlush, BufferedWriter flushWriter, String previous) throws IOException;

    void flushRemainingContent(T toFlush, BufferedWriter flushWriter) throws IOException;

    String getFinalFileName();

    String getRefFileName();

    String getLineRefID(String line);

    Configuration getConfig();
}
