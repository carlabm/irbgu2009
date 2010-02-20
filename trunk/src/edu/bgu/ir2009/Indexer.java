package edu.bgu.ir2009;

import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.DocumentPostings;
import edu.bgu.ir2009.auxiliary.InMemoryDocs;
import edu.bgu.ir2009.auxiliary.UpFacade;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
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

    private final Map<String, Map<String, Double>> documentsVectors = new HashMap<String, Map<String, Double>>();
    private final List<DocumentPostings> postings = Collections.synchronizedList(new LinkedList<DocumentPostings>());
    private final ExecutorService executor;
    private final Configuration config;
    private final Object lock = new Object();

    private Parser parser;
    private boolean isStarted = false;
    private int indexedDocs = 0;
    private int totalIndexedDocs = 0;
    private InMemoryDocs inMemoryDocs;
    private int toFlushPostings = 0;

    public Indexer(String docsDir, String srcStopWordsFileName, boolean useStemmer) throws IOException {
        this(new Configuration(docsDir, srcStopWordsFileName, useStemmer));
    }

    public Indexer(Configuration config) throws IOException {
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

    public CountDownLatch start() throws XMLStreamException, FileNotFoundException {
        final CountDownLatch res = new CountDownLatch(1);
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
                DocumentPostings postings;
                while (!executor.isShutdown() && (postings = parser.getNextParsedDocumentPostings()) != null) {
                    synchronized (eventsLock) {
                        totalIndexedDocs++;
                    }
                    executor.execute(new IndexerWorker(postings));
                }
                parser = null;
                if (!executor.isShutdown()) {
                    new Thread(new Runnable() {
                        public void run() {
                            executor.shutdown();
                            try {
                                executor.awaitTermination(10, TimeUnit.DAYS);

/*                                doPreProcessing();
Indexer.this.postings.clear();
System.gc();
memoryIndex = PostingFileUtils.saveIndex(index, documentsVectors, config);
index.clear();
documentsVectors.clear();
System.gc();*/
                                res.countDown();
                            } catch (InterruptedException e) {
                                logger.warn(e, e);
                            } catch (Exception e) {
                                logger.error(e, e);
                            }
                        }
                    }).start();
                } else {
//                    index.clear();
                }
            }
        });
        return res;
    }

    public InMemoryDocs getInMemoryDocs() {
        return inMemoryDocs;
    }

    public void stop() {
        executor.shutdownNow();
        parser.stop();
        parser = null;
    }

    private class IndexerWorker implements Runnable {
        private final DocumentPostings docPostings;

        public IndexerWorker(DocumentPostings docPostings) {
            this.docPostings = docPostings;
        }

        public void run() {
            postings.add(docPostings);
            synchronized (eventsLock) {
                indexedDocs++;
                UpFacade.getInstance().addIndexerEvent(indexedDocs, totalIndexedDocs);
            }
        }
    }
}
