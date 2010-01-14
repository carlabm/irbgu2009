package edu.bgu.ir2009;

import edu.bgu.ir2009.auxiliary.*;
import edu.bgu.ir2009.auxiliary.io.DocumentWriter;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 19:00:01
 */
public class Parser {
    private final static Logger logger = Logger.getLogger(Parser.class);
    private final static DocumentPostings EMPTY_POSTING = new DocumentPostings();
    private final static Object lock = new Object();

    private final Set<String> stopWordsSet = new HashSet<String>();
    private final boolean useStemmer;
    private final ExecutorService executor;
    private final BlockingQueue<DocumentPostings> docPostings = new LinkedBlockingQueue<DocumentPostings>();
    private final NextWordIndex nextWordIndex;
    private final DocumentWriter docWriter;

    private boolean isStartable = true;
    private ReadFile reader;
    private Stemmer stemmer;
    private int totalUnParsedDocuments = 0;
    private int totalParsedDocuments = 0;

    public Parser(Configuration config) throws IOException {
        this(config, true);
    }

    public Parser(Configuration config, boolean createReader) throws IOException {
        this(createReader ? new ReadFile(config) : null, config);
    }

    public Parser(ReadFile reader, Configuration config) throws IOException {
        this.reader = reader;
        useStemmer = config.useStemmer();
        if (useStemmer) {
            stemmer = new Stemmer();
        }
        BufferedReader stopWordsReader;
        String stopWordsFileName = config.getSrcStopWordsFileName();
        try {
            stopWordsReader = new BufferedReader(new FileReader(stopWordsFileName));
            String stopWord;
            try {
                while ((stopWord = stopWordsReader.readLine()) != null) {
                    stopWordsSet.add(stopWord.trim());
                }
            } catch (IOException e) {
                logger.error(e, e);
            } finally {
                try {
                    stopWordsReader.close();
                } catch (IOException ignored) {
                }
            }
        } catch (FileNotFoundException e) {
            logger.warn("The stop-words file '" + stopWordsFileName + "' not found! Using non!");
        }
        if (reader != null) {
            executor = Executors.newFixedThreadPool(config.getParserThreadsCount());
            docWriter = new DocumentWriter(config);
            nextWordIndex = new NextWordIndex(config, false);
        } else {
            executor = null;
            docWriter = null;
            nextWordIndex = null;
            isStartable = false;
        }
    }

    public void start() throws XMLStreamException, FileNotFoundException {
        synchronized (this) {
            if (isStartable) {
                reader.start();
                isStartable = false;
            } else {
                throw new IllegalStateException("This parser cannot be started! (Already started or in query mode)");
            }
        }
        executor.execute(new Runnable() {
            public void run() {
                UnParsedDocument doc;
                while (!executor.isShutdown() && (doc = reader.getNextDocument()) != null) {
                    synchronized (lock) {
                        totalUnParsedDocuments++;
                    }
                    logger.info("Document " + doc.getDocNo() + " is being submitted for parsing...");
                    executor.execute(new ParserWorker(doc));
                }
                reader = null;
                new Thread(new Runnable() {
                    public void run() {
                        executor.shutdown();
                        try {
                            executor.awaitTermination(10, TimeUnit.DAYS);
                        } catch (InterruptedException e) {
                            logger.warn(e, e);
                        } finally {
                            try {
                                docWriter.close();
                            } catch (IOException e) {
                                logger.error(e, e);
                            }
                            try {
                                nextWordIndex.store();
                            } catch (IOException e) {
                                logger.error(e, e);
                            }
                            try {
                                logger.debug("Finished parsing. Parsed " + totalParsedDocuments + " Documents");
                                docPostings.put(EMPTY_POSTING);
                            } catch (InterruptedException e) {
                                logger.warn(e, e);
                            }
                        }
                    }
                }).start();
            }
        });
    }

    public DocumentPostings getNextParsedDocumentPostings() {
        DocumentPostings res = null;
        try {
            res = docPostings.take();
            if (res.getDocNo() == null) {
                res = null;
            }
        } catch (InterruptedException e) {
            logger.warn(e, e);
        }
        return res;
    }

    public DocumentPostings parse(String id, String text) {
        UnParsedDocument doc = new UnParsedDocument();
        doc.setDocNo(id);
        doc.setText(text);
        return parse(doc, false);
    }


    private DocumentPostings parse(UnParsedDocument unParsedDoc, boolean addToQueue) {
        logger.info("Started parsing document " + unParsedDoc.getDocNo());
        DocumentPostings res = new DocumentPostings(unParsedDoc.getDocNo());
        long pos = 0;
        StringBuilder currTerm = new StringBuilder();
        char[] docChars = unParsedDoc.getText().toCharArray();
        String lastTerm = null;
        for (char readChar : docChars) {
            if (Character.isLetter(readChar)) {
                currTerm.append(Character.toLowerCase(readChar));
            } else {
                if (currTerm.length() > 0) {
                    String newTerm = currTerm.toString();
                    if (!stopWordsSet.contains(newTerm)) {
                        if (useStemmer) {
                            stemmer.add(newTerm.toCharArray(), newTerm.length());
                            stemmer.stem();
                            newTerm = stemmer.toString();
                            stemmer = new Stemmer();
                        }
                        res.addTerm(newTerm, pos);
                        if (lastTerm != null) {
                            nextWordIndex.addWordPair(res.getDocNo(), lastTerm, newTerm, pos - 1);
                        }
                        lastTerm = newTerm;
                        pos++;
                    }
                    currTerm.delete(0, currTerm.length());
                }
            }
        }
        if (addToQueue) {
            try {
                docPostings.put(res);
            } catch (InterruptedException e) {
                logger.warn(e, e);
            }
        }
        logger.info("Finished parsing document " + unParsedDoc.getDocNo());
        return res;
    }

    public void stop() {
        reader.stop();
        reader = null;
        executor.shutdownNow();
        try {
            docWriter.close();
        } catch (IOException e) {
            logger.error(e, e);
        }
    }

    private class ParserWorker implements Runnable {
        private final UnParsedDocument doc;

        public ParserWorker(UnParsedDocument doc) {
            this.doc = doc;
        }

        public void run() {
            parse(doc, true);
            try {
                docWriter.write(doc);
            } catch (IOException e) {
                logger.error(e, e);
            }
            synchronized (lock) {
                totalParsedDocuments++;
                UpFacade.getInstance().addParserEvent(totalParsedDocuments, totalUnParsedDocuments);
            }
        }
    }

    public static void main(String[] args) throws XMLStreamException, IOException, InterruptedException {
        BasicConfigurator.configure();
        Parser parser = new Parser(new Configuration("FT933", "stop-words.txt", true));
        parser.start();
        while (parser.getNextParsedDocumentPostings() != null) {

        }
    }
}
