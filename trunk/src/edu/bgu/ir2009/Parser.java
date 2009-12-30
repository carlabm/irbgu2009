package edu.bgu.ir2009;

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
    private final static ParsedDocument emptyParsedDoc = new ParsedDocument(new UnParsedDocument());

    private final Set<String> stopWordsSet = new HashSet<String>();
    private final boolean useStemmer;
    private final ReadFile reader;
    private final ExecutorService executor;

    private final BlockingQueue<ParsedDocument> parsedDocs = new LinkedBlockingQueue<ParsedDocument>();

    private boolean isStarted = false;
    private Stemmer stemmer;

    public Parser(Configuration config) {
        this(new ReadFile(config), config);
    }

    public Parser(ReadFile reader, Configuration config) {
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
        executor = Executors.newFixedThreadPool(config.getParserThreadsCount());
    }

    public void start() throws XMLStreamException, FileNotFoundException {
        synchronized (this) {
            if (!isStarted) {
                reader.start();
                isStarted = true;
            } else {
                throw new IllegalStateException("cannot start same parser twice");
            }
        }
        executor.execute(new Runnable() {
            public void run() {
                UnParsedDocument doc;
                while ((doc = reader.getNextDocument()) != null) {
                    logger.info("Document " + doc.getDocNo() + " is being submitted for parsing...");
                    executor.execute(new ParserWorker(doc));
                }
                new Thread(new Runnable() {
                    public void run() {
                        executor.shutdown();
                        try {
                            executor.awaitTermination(10, TimeUnit.DAYS);
                            logger.debug("putting empty parsed doc in queue");
                            parsedDocs.put(emptyParsedDoc);
                        } catch (InterruptedException e) {
                            logger.warn(e, e);
                        }
                    }
                }).start();
            }
        });
    }

    public ParsedDocument getNextParsedDocument() {
        ParsedDocument res = null;
        try {
            res = parsedDocs.take();
            if (res.getDocNo() == null) {
                res = null;
            }
        } catch (InterruptedException e) {
            logger.warn(e, e);
        }
        return res;
    }

    public ParsedDocument parse(String docNo, String text) {
        UnParsedDocument doc = new UnParsedDocument();
        doc.setDocNo(docNo);
        doc.setText(text);
        return parse(doc, false);
    }


    private ParsedDocument parse(UnParsedDocument unParsedDoc, boolean addToQueue) {
        logger.info("Started parsing document " + unParsedDoc.getDocNo());
        ParsedDocument res = new ParsedDocument(unParsedDoc);
        long pos = 0;
        StringBuilder currTerm = new StringBuilder();
        char[] docChars = unParsedDoc.getText().toCharArray();
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
                        pos++;
                    }
                    currTerm.delete(0, currTerm.length());
                }
            }
        }
        if (addToQueue) {
            try {
                parsedDocs.put(res);
            } catch (InterruptedException e) {
                logger.warn(e, e);
            }
        }
        logger.info("Finished parsing document " + unParsedDoc.getDocNo());
        return res;
    }

    private class ParserWorker implements Runnable {
        private final UnParsedDocument doc;

        public ParserWorker(UnParsedDocument doc) {
            this.doc = doc;
        }

        public void run() {
            parse(doc, true);
        }
    }
}
