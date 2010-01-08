package edu.bgu.ir2009;

import edu.bgu.ir2009.auxiliary.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 25/12/2009
 * Time: 16:20:46
 */
public class Indexer {
    private static final Logger logger = Logger.getLogger(Indexer.class);
    private static final Object eventsLock = new Object();
    private final Map<String, TermData> index = new HashMap<String, TermData>();
    private final Set<ParsedDocument> docsCache = new HashSet<ParsedDocument>();
    private final ExecutorService executor;
    private Parser parser;
    private final Configuration config;
    private final Object lock = new Object();

    private boolean isStarted = false;
    private int indexedDocs = 0;
    private int totalIndexedDocs = 0;
    private InMemoryIndex memoryIndex;
    private InMemoryDocs inMemoryDocs;

    public Indexer(String docsDir, String srcStopWordsFileName, boolean useStemmer) {
        this(new Configuration(docsDir, srcStopWordsFileName, useStemmer));
    }

    public Indexer(Configuration config) {
        this(new Parser(config), config);
    }

    public Indexer(Parser parser, Configuration config) {
        this.parser = parser;
        this.config = config;
        executor = Executors.newFixedThreadPool(config.getIndexerThreadsCount());
    }

    public Configuration getConfig() {
        return config;
    }

    public void start() throws XMLStreamException, FileNotFoundException {
        synchronized (this) {
            if (!isStarted) {
                parser.start();
                isStarted = true;
            } else {
                throw new IllegalStateException("cannot start same indexer twice");
            }
        }
        executor.execute(new Runnable() {
            public void run() {
                ParsedDocument doc;
                while (!executor.isShutdown() && (doc = parser.getNextParsedDocument()) != null) {
                    synchronized (eventsLock) {
                        totalIndexedDocs++;
                    }
                    executor.execute(new IndexerWorker(doc));
                }
                parser = null;
                if (!executor.isShutdown()) {
                    new Thread(new Runnable() {
                        public void run() {
                            executor.shutdown();
                            try {
                                executor.awaitTermination(10, TimeUnit.DAYS);
                                doPreProcessing();
                                memoryIndex = PostingFileUtils.saveIndex(index, config);
                                inMemoryDocs = PostingFileUtils.saveParsedDocuments(docsCache, config);
                                docsCache.clear();
                                index.clear();
                                System.gc();
                            } catch (InterruptedException e) {
                                logger.warn(e, e);
                            } catch (Exception e) {
                                logger.error(e, e);
                            }
                        }
                    }).start();
                } else {
                    docsCache.clear();
                    index.clear();
                }
            }
        });
    }

    private void doPreProcessing() {
        long totalDocs = docsCache.size();
        for (TermData term : index.values()) {
            term.setTotalDocs(totalDocs);
        }
        for (ParsedDocument doc : docsCache) {
            doc.finalizeDocument(index);
        }
    }

    public InMemoryIndex getMemoryIndex() {
        return memoryIndex;
    }

    public InMemoryDocs getInMemoryDocs() {
        return inMemoryDocs;
    }

    public void stop() {
        executor.shutdownNow();
        parser.stop();
        parser = null;
    }

    private void indexParsedDocument(ParsedDocument doc) throws IOException {
        logger.info("Starting indexing doc: " + doc.getDocNo());
        String docNo = doc.getDocNo();
        Map<String, Set<Long>> docTerms = doc.getTerms();
        for (String term : docTerms.keySet()) {
            TermData termData;
            synchronized (lock) {
                termData = index.get(term);
                if (termData == null) {
                    termData = new TermData(term);
                    index.put(term, termData);
                }
            }
            termData.addPosting(docNo, docTerms.get(term));
        }
        docsCache.add(doc);
        logger.info("Finished indexing doc: " + doc.getDocNo());
    }

    private class IndexerWorker implements Runnable {
        private final ParsedDocument doc;

        public IndexerWorker(ParsedDocument doc) {
            this.doc = doc;
        }

        public void run() {
            try {
                indexParsedDocument(doc);
                synchronized (eventsLock) {
                    indexedDocs++;
                    UpFacade.getInstance().addIndexerEvent(indexedDocs, totalIndexedDocs);
                }
            } catch (IOException e) {
                logger.error(e, e);
            }
        }
    }

    public static void main(String[] args) throws IOException, XMLStreamException {
        BasicConfigurator.configure();
        Indexer indexer = new Indexer("tmp", "stop-words.txt", true);
        indexer.start();
        int i = 0;
    }
}
