package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.UnParsedDocument;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 21/02/2010
 * Time: 19:58:14
 */
public class DocumentIndex {
    private final static Logger logger = Logger.getLogger(DocumentIndex.class);
    private final FlushWriter<List<UnParsedDocument>> writer;
    private final List<UnParsedDocument> docs = new LinkedList<UnParsedDocument>();
    private int unflushedDocs = 0;

    public DocumentIndex(Configuration config) {
        writer = new FlushWriter<List<UnParsedDocument>>(new DocumentFlushingStrategy(config));
    }

    public synchronized void addDocument(UnParsedDocument doc) throws IOException {
        docs.add(doc);
        unflushedDocs++;
        if (unflushedDocs > 3000) {
            unflushedDocs = 0;
            logger.info("Flushing saved documents...");
            writer.flush(docs);
            logger.info("Done flushing saved documents...");
        }
    }

    public synchronized void close() throws IOException {
        logger.info("Flushing last saved documents...");
        writer.flush(docs);
        writer.close();
    }
}
