package edu.bgu.ir2009;

import edu.bgu.ir2009.auxiliary.*;
import edu.bgu.ir2009.auxiliary.io.DocumentVectorsFlushingStrategy;
import edu.bgu.ir2009.auxiliary.io.FlushWriter;
import edu.bgu.ir2009.gui.IndexingDialog;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 17/02/2010
 * Time: 18:29:40
 */
public class IndexerV2 {
    private static final Logger logger = Logger.getLogger(IndexerV2.class);
    private final ExecutorService executor;
    private final Object eventsLock = new Object();
    private final Object postingsWritingLock = new Object();

    private BufferedWriter docPostingsWriter;
    private Parser parser;
    private boolean isStarted = false;
    private int totalIndexedDocs = 0;
    private int indexedDocs = 0;
    private Configuration config;
    private TermIndex termIndex;

    public IndexerV2(String docsDir, String srcStopWordsFileName, boolean useStemmer) throws IOException {
        this(new Configuration(docsDir, srcStopWordsFileName, useStemmer));
    }

    public IndexerV2(Configuration config) throws IOException {
        this(new Parser(config), config);
    }

    public IndexerV2(Parser parser, Configuration config) {
        this.parser = parser;
        this.config = config;
        executor = Executors.newFixedThreadPool(config.getIndexerThreadsCount());
        termIndex = new TermIndex(config);
        try {
            docPostingsWriter = new BufferedWriter(new FileWriter(config.getPostingsFileName()));
        } catch (IOException e) {
            logger.error(e, e);
        }
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
                                res.countDown();
                                termIndex.close();
                                docPostingsWriter.close();
                                Map<String, Long> termDFMap = PostingFileUtils.getTermDFMap(config);
                                FlushWriter docVectorsWriter = new FlushWriter(new DocumentVectorsFlushingStrategy(config, termDFMap, totalIndexedDocs));
                                docVectorsWriter.flush(null);
                                docVectorsWriter.close();
                                config.setTotalDocuments(totalIndexedDocs);
                            } catch (InterruptedException e) {
                                logger.warn(e, e);
                            } catch (Exception e) {
                                logger.error(e, e);
                            }
                        }
                    }).start();
                }
            }
        });
        return res;
    }

    private class IndexerWorker implements Runnable {
        private final DocumentPostings docPostings;

        public IndexerWorker(DocumentPostings docPostings) {
            this.docPostings = docPostings;
        }

        public void run() {
            try {
                termIndex.addPostings(docPostings);
                if (docPostingsWriter != null) {
                    synchronized (postingsWritingLock) {
                        docPostingsWriter.write(docPostings.serialize() + "\n");
                    }
                }
                synchronized (eventsLock) {
                    indexedDocs++;
                    UpFacade.getInstance().addIndexerEvent(indexedDocs, totalIndexedDocs);
                }
            } catch (IOException e) {
                logger.error(e, e);
            }
        }
    }


    public static void main(String[] args) throws IOException, XMLStreamException, InterruptedException {
        BasicConfigurator.configure();
        Configuration conf = new Configuration("FT933", "stop-words.txt", true, 50, 1.0, 2.0, 2, 2, 2);
        IndexingDialog dialog = new IndexingDialog();
        IndexerV2 indexer = new IndexerV2(conf);
        CountDownLatch countDownLatch = indexer.start();
        dialog.pack();
        dialog.setVisible(true);
        countDownLatch.await();
        int i = 0;
    }

}
