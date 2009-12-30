package edu.bgu.ir2009;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
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
    private final Map<String, TermData> index = new HashMap<String, TermData>();
    private final Set<ParsedDocument> docsCache = new HashSet<ParsedDocument>();
    private final PostingFileUtils postingFileUtils;
    private final ExecutorService executor;
    private final Parser parser;
    private final Configuration config;
    private final Object lock = new Object();

    public Indexer(String docsDir, String srcStopWordsFileName, boolean useStemmer) {
        this(new Configuration(docsDir, srcStopWordsFileName, useStemmer));
    }

    public Indexer(Configuration config) {
        this(new Parser(config), config);
    }

    public Indexer(Parser parser, Configuration config) {
        this.parser = parser;
        this.config = config;
        postingFileUtils = new PostingFileUtils(config);
        executor = Executors.newFixedThreadPool(config.getIndexerThreadsCount());
    }

    public void start() {
        executor.execute(new Runnable() {
            public void run() {
                ParsedDocument doc;
                while ((doc = parser.getNextParsedDocument()) != null) {
                    executor.execute(new IndexerWorker(doc));
                }
                new Thread(new Runnable() {
                    public void run() {
                        executor.shutdown();
                        try {
                            executor.awaitTermination(10, TimeUnit.DAYS);
                            postingFileUtils.saveIndex(index);
                        } catch (InterruptedException e) {
                            logger.warn(e, e);
                        } catch (Exception e) {
                            logger.error(e, e);
                        }
                    }
                }).start();
            }
        });
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
            } catch (IOException e) {
                logger.error(e, e);
            }
        }
    }

    public static void main(String[] args) throws IOException, XMLStreamException {
        BasicConfigurator.configure();
        Configuration configuration = new Configuration("project.cfg");
        ReadFile readFile = new ReadFile(configuration);
        Parser parser = new Parser(readFile, configuration);
        readFile.start();
        parser.start();
        Indexer indexer = new Indexer(parser, configuration);
        indexer.start();
        int i = 0;
    }
}
