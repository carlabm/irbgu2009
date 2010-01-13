package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.UnParsedDocument;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 13/01/2010
 * Time: 17:21:16
 */
public class DocumentReader {
    private final Configuration config;
    private final RandomAccessFile docFile;

    public DocumentReader(Configuration config) throws FileNotFoundException {
        this.config = config;
        docFile = new RandomAccessFile(config.getSavedDocsFileName(), "r");
    }

    public Map<String, Long> readRefFile() throws IOException {
        Map<String, Long> res = new HashMap<String, Long>();
        LineIterator iterator = null;
        try {
            iterator = FileUtils.lineIterator(new File(config.getSavedDocsRefFileName()));
            while (iterator.hasNext()) {
                String line = iterator.nextLine();
                int end = line.indexOf(':');
                String docNo = line.substring(0, end);
                res.put(docNo, Long.parseLong(line.substring(end + 1, line.length())));
            }
        } finally {
            LineIterator.closeQuietly(iterator);
        }
        return res;
    }

    public UnParsedDocument read(Long offset) throws IOException {
        docFile.seek(offset);
        return new UnParsedDocument(docFile.readLine());
    }

    public void close() throws IOException {
        docFile.close();
    }
}
