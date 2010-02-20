package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: Jan 27, 2010
 * Time: 11:43:25 PM
 */
public class FlushWriter<T> {
    private final FlushingStrategy<T> fs;
    private final Configuration config;
    private final Object lock = new Object();
    private File flushFile;
    private int flushNum = 0;

    public FlushWriter(FlushingStrategy<T> fs) {
        this.fs = fs;
        this.config = fs.getConfig();
    }

    public void flush(T toFlush) throws IOException {
        synchronized (lock) {
            File oldFlushIndexFile = null;
            if (flushFile != null) {
                oldFlushIndexFile = flushFile;
            }
            flushFile = new File(config.getWorkingDir() + "/" + fs.getTempFilePrefix() + flushNum++);
            BufferedWriter flushWriter = null;
            try {
                flushWriter = new BufferedWriter(new FileWriter(flushFile));
                if (oldFlushIndexFile != null) {
                    LineIterator iterator = FileUtils.lineIterator(oldFlushIndexFile);
                    while (iterator.hasNext()) {
                        String previous = iterator.nextLine();
                        if ("".equals(previous)) {
                            break;
                        }
                        fs.mergePreviousWithNew(toFlush, flushWriter, previous);
                        flushWriter.write('\n');
                    }
                    LineIterator.closeQuietly(iterator);
                    FileUtils.deleteQuietly(oldFlushIndexFile);
                }
                fs.flushRemainingContent(toFlush, flushWriter);
            } finally {
                if (flushWriter != null) {
                    try {
                        flushWriter.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }


    public void close() throws IOException {
        synchronized (lock) {
            if (flushFile != null) {
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(fs.getRefFileName()));
                    LineIterator iterator = FileUtils.lineIterator(flushFile);
                    long pos = 0;
                    while (iterator.hasNext()) {
                        String line = iterator.nextLine();
                        String refId = fs.getLineRefID(line);
                        writer.write(refId + ':' + pos + ':' + line.length() + '\n');
                        pos += line.length() + 1;
                    }
                    LineIterator.closeQuietly(iterator);
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }

                File nwFile = new File(fs.getFinalFileName());
                boolean success = flushFile.renameTo(nwFile);
                if (!success) {
                    throw new RuntimeException("Could not rename nw flush file to real file");
                }
            } else {
                throw new RuntimeException("Cannot close a next word writer that has never been flushed");
            }
        }
    }
}
