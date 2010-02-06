package edu.bgu.ir2009;

import edu.bgu.ir2009.auxiliary.*;
import edu.bgu.ir2009.gui.IndexingDialog;
import org.apache.log4j.BasicConfigurator;
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
    private final Map<String, TermData> index = new HashMap<String, TermData>();
    private final Map<String, Map<String, Double>> documentsVectors = new HashMap<String, Map<String, Double>>();
    private final List<DocumentPostings> postings = Collections.synchronizedList(new LinkedList<DocumentPostings>());
    private final ExecutorService executor;
    private final Configuration config;
    private final Object lock = new Object();

    private Parser parser;
    private boolean isStarted = false;
    private int indexedDocs = 0;
    private int totalIndexedDocs = 0;
    private InMemoryIndex memoryIndex;
    private InMemoryDocs inMemoryDocs;

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
                                doPreProcessing();
                                Indexer.this.postings.clear();
                                System.gc();
                                memoryIndex = PostingFileUtils.saveIndex(index, documentsVectors, config);
                                index.clear();
                                documentsVectors.clear();
                                System.gc();
                                res.countDown();
                            } catch (InterruptedException e) {
                                logger.warn(e, e);
                            } catch (Exception e) {
                                logger.error(e, e);
                            }
                        }
                    }).start();
                } else {
                    index.clear();
                }
            }
        });
        return res;
    }

    private void doPreProcessing() {
        for (TermData term : index.values()) {
            term.setTotalDocs(totalIndexedDocs);
        }
        for (DocumentPostings docPostings : postings) {
            Map<String, Double> docVector = calculateDocumentVector(index, docPostings);
            documentsVectors.put(docPostings.getDocNo(), docVector);
        }
    }

    public static Map<String, Double> calculateDocumentVector(Map<String, TermData> index, DocumentPostings doc) {
        double docLength = 0.0;
        Map<String, Set<Long>> terms = doc.getTerms();
        for (String term : terms.keySet()) {
            int termFreq = terms.get(term).size();
            double td_idf = index.get(term).getIdf() * termFreq;
            docLength += td_idf * td_idf;
        }
        docLength = Math.sqrt(docLength);
        Map<String, Double> documentVector = new HashMap<String, Double>();
        for (String term : terms.keySet()) {
            int termFreq = terms.get(term).size();
            documentVector.put(term, termFreq / docLength);
        }
        return documentVector;
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

    private void indexParsedDocument(DocumentPostings postings) {
        String docNo = postings.getDocNo();
        Map<String, Set<Long>> docTerms = postings.getTerms();
        for (String term : docTerms.keySet()) {
            TermData termData;
            synchronized (lock) {
                termData = index.get(term);
                if (termData == null) {
                    termData = new TermData(term);
                    index.put(term, termData);
                }
                //todo add here a strtegy
            }
            termData.addPosting(docNo, docTerms.get(term));
        }
    }

    private class IndexerWorker implements Runnable {
        private final DocumentPostings docPostings;

        public IndexerWorker(DocumentPostings docPostings) {
            this.docPostings = docPostings;
        }

        public void run() {
            indexParsedDocument(docPostings);
            postings.add(docPostings);
            synchronized (eventsLock) {
                indexedDocs++;
                UpFacade.getInstance().addIndexerEvent(indexedDocs, totalIndexedDocs);
            }
        }
    }

    public static void main(String[] args) throws IOException, XMLStreamException, InterruptedException {
        BasicConfigurator.configure();
        Configuration conf = new Configuration("FT933", "stop-words.txt", true, 50, 1.0, 2.0, 2, 2, 2);
        IndexingDialog dialog = new IndexingDialog();
        Indexer indexer = new Indexer(conf);
        UpFacade.getInstance().addIndexBindEvent(indexer);
        CountDownLatch countDownLatch = indexer.start();
        dialog.pack();
        dialog.setVisible(true);
        countDownLatch.await();
        InMemoryIndex memoryIndex = new InMemoryIndex(indexer.getConfig(), true);
        memoryIndex.load();
        TermData termData = memoryIndex.getTermData("justif");
        TermData termData2 = indexer.getMemoryIndex().getTermData("justif");
        logger.info("term data equality" + termData.equals(termData2));
        for (String docPostings : termData.getPostingsMap().keySet()) {
            Set<String> fakeSet = new Set<String>() {
                public int size() {
                    return 0;
                }

                public boolean isEmpty() {
                    return false;
                }

                public boolean contains(Object o) {
                    return true;
                }

                public Iterator<String> iterator() {
                    return null;
                }

                public Object[] toArray() {
                    return new Object[0];
                }

                public <T> T[] toArray(T[] a) {
                    return null;
                }

                public boolean add(String s) {
                    return false;
                }

                public boolean remove(Object o) {
                    return false;
                }

                public boolean containsAll(Collection<?> c) {
                    return false;
                }

                public boolean addAll(Collection<? extends String> c) {
                    return false;
                }

                public boolean retainAll(Collection<?> c) {
                    return false;
                }

                public boolean removeAll(Collection<?> c) {
                    return false;
                }

                public void clear() {

                }
            };
            Map<String, Double> map = memoryIndex.getDocumentVector(docPostings);
            Map<String, Double> map2 = indexer.getMemoryIndex().getDocumentVector(docPostings);
            logger.info(map.equals(map2));
        }
        int i = 0;
    }
}
