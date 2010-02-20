package edu.bgu.ir2009.auxiliary;

import edu.bgu.ir2009.auxiliary.io.FlushWriter;
import edu.bgu.ir2009.auxiliary.io.TermIndexFlushingStrategy;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 15/02/2010
 * Time: 20:28:47
 */
public class TermIndex {
    private static final Logger logger = Logger.getLogger(TermIndex.class);
    private final Map<String, TermData> index = new HashMap<String, TermData>();
    private final FlushWriter<Map<String, TermData>> termWriter;

    private int inMemoryPostings = 0;

    public TermIndex(Configuration config) {
        this.termWriter = new FlushWriter<Map<String, TermData>>(new TermIndexFlushingStrategy(config));
    }

    public synchronized void addPostings(DocumentPostings postings) throws IOException {
        String docNo = postings.getDocNo();
        Map<String, Set<Long>> docTerms = postings.getTerms();
        for (String term : docTerms.keySet()) {
            TermData termData = index.get(term);
            if (termData == null) {
                termData = new TermData(term);
                index.put(term, termData);
            }
            inMemoryPostings += termData.getPostingsMap().size();
            if (inMemoryPostings > 10000000) {
                logger.info("Flushing terms to disk...");
                inMemoryPostings = 0;
                termWriter.flush(index);
            }
            termData.addPosting(docNo, docTerms.get(term));
        }
    }

    public synchronized void close() throws IOException {
        logger.info("Finishing flushing terms to disk...");
        termWriter.flush(index);
        termWriter.close();
    }
}
