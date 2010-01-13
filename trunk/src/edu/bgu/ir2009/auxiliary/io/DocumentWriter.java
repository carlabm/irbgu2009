package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.InMemoryDocs;
import edu.bgu.ir2009.auxiliary.UnParsedDocument;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 13/01/2010
 * Time: 14:30:33
 */
public class DocumentWriter {
    private final static Logger logger = Logger.getLogger(DocumentWriter.class);
    private final Configuration config;
    private final BufferedWriter docWriter;
    private final BufferedWriter refWriter;
    private final InMemoryDocs memoryDocs;
    private final Object lock = new Object();
    private long offset;

    public DocumentWriter(Configuration config) throws IOException {
        this.config = config;
        docWriter = new BufferedWriter(new FileWriter(config.getSavedDocsFileName()));
        refWriter = new BufferedWriter(new FileWriter(config.getSavedDocsRefFileName()));
        memoryDocs = new InMemoryDocs(config);
        offset = 0;
    }

    public void write(UnParsedDocument doc) throws IOException {
        String serialized = doc.serialize();
        synchronized (lock) {
            docWriter.write(serialized + '\n');
            memoryDocs.addDocument(doc.getDocNo(), offset);
            refWriter.write(doc.getDocNo() + ":" + offset + "\n");
            offset += serialized.length() + 1;
        }
    }

    public InMemoryDocs getMemoryDocs() {
        return memoryDocs;
    }

    public void close() throws IOException {
        docWriter.close();
        refWriter.close();
    }
}
