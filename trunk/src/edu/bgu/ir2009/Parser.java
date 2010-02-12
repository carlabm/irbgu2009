package edu.bgu.ir2009;

import edu.bgu.ir2009.auxiliary.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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

    private boolean isStartable = true;
    private ReadFile reader;
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
        String stopWordsFileName = config.getSrcStopWordsFileName();
        try {
            FileChannel channel = new FileInputStream(stopWordsFileName).getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            StringBuilder builder = new StringBuilder();
            while (buffer.remaining() > 0) {
                char singleChar = (char) buffer.get();
                if (singleChar != '\n') {
                    builder.append(singleChar);
                } else {
                    stopWordsSet.add(builder.toString());
                    builder.delete(0, builder.length());
                }
            }
            channel.close();
        } catch (FileNotFoundException e) {
            logger.warn("The stop-words file '" + stopWordsFileName + "' not found! Using non!");
        }
        if (reader != null) {
            executor = Executors.newFixedThreadPool(config.getParserThreadsCount());
            nextWordIndex = new NextWordIndex(config);
        } else {
            executor = null;
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
                                nextWordIndex.close();
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
                            Stemmer stemmer = new Stemmer();
                            stemmer.add(newTerm.toCharArray(), newTerm.length());
                            stemmer.stem();
                            newTerm = stemmer.toString();
                        }
                        res.addTerm(newTerm, pos);
                        if (lastTerm != null && addToQueue) {
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
        return res;
    }

    public void stop() {
        reader.stop();
        reader = null;
        executor.shutdownNow();
    }

    private class ParserWorker implements Runnable {
        private final UnParsedDocument doc;

        public ParserWorker(UnParsedDocument doc) {
            this.doc = doc;
        }

        public void run() {
            parse(doc, true);
            synchronized (lock) {
                totalParsedDocuments++;
                UpFacade.getInstance().addParserEvent(totalParsedDocuments, totalUnParsedDocuments);
            }
        }
    }

    public static void main(String[] args) throws XMLStreamException, IOException, InterruptedException {
        BasicConfigurator.configure();
        Parser parser = new Parser(new Configuration("FT933", "stop-words.txt", true, 45, 1.0, 2.0, 2, 2, 1));
        parser.start();
        while (parser.getNextParsedDocumentPostings() != null) {

        }
    }
}
