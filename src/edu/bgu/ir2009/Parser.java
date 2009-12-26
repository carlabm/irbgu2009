package edu.bgu.ir2009;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 19:00:01
 */
public class Parser {
    private static Logger logger = Logger.getLogger(Parser.class);

    private final Set<String> stopWordsSet = new HashSet<String>();
    private BlockingQueue<ParsedDocument> parsedDocs = new LinkedBlockingQueue<ParsedDocument>();
    private boolean useStemmer;
    private Stemmer stemmer;
    private final ParsedDocument emptyParsedDoc = new ParsedDocument(new DocumentReader());

    public Parser(Configuration config) throws FileNotFoundException {
        String stopWordsFileName = config.getStopWordsFileName();
        useStemmer = config.useStemmer();
        if (useStemmer) {
            stemmer = new Stemmer();
        }
        BufferedReader reader = new BufferedReader(new FileReader(stopWordsFileName));
        String stopWord = "";
        try {
            while ((stopWord = reader.readLine()) != null) {
                stopWordsSet.add(stopWord.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        Configuration config = new Configuration("project.cfg");
        final Parser parser = new Parser(config);
        final ReadFile readFile = new ReadFile("FT933");
        new Thread(new Runnable() {
            public void run() {
                try {
                    readFile.start("FT933_1");
                    readFile.setDoneReading();
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        final Indexer indexer = new Indexer(config);
        new Thread(new Runnable() {
            public void run() {
                ParsedDocument parsedDoc;
                while ((parsedDoc = parser.getNextParsedDocument()) != null) {
                    try {
                        indexer.addParsedDocument(parsedDoc);
                    } catch (IOException e) {
                        logger.error(e, e);
                    }
                }
                int i = 0;
            }
        }).start();
        DocumentReader nextDoc;
        while ((nextDoc = readFile.getNextDocument()) != null) {
            parser.parse(nextDoc);
        }
        parser.setDoneParsing();
        int j = 0;
    }

    public void setDoneParsing() {
        try {
            parsedDocs.put(emptyParsedDoc);
        } catch (InterruptedException e) {
            logger.warn(e, e);
        }
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

    public void parse(DocumentReader docReader) {
        try {
            parsedDocs.put(private_parse(docReader));
        } catch (InterruptedException e) {
            logger.warn(e, e);
        }
    }

    private ParsedDocument private_parse(DocumentReader docReader) {
        ParsedDocument res = new ParsedDocument(docReader);
        long pos = 0;
        char readChar;
        StringBuilder currTerm = new StringBuilder();
        while ((readChar = docReader.read()) != '\0') {
            if (!Character.isWhitespace(readChar) && !Character.isSpaceChar(readChar)) {
                currTerm.append(readChar);
            } else {
                if (currTerm.length() > 0) {
                    String newTerm = buildTerm(currTerm);
                    if (!"".equals(newTerm) && !stopWordsSet.contains(newTerm)) {
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
        return res;
    }

    private String buildTerm(StringBuilder currTerm) {
        while (currTerm.length() > 0 && !Character.isLetter(currTerm.charAt(0)) && !Character.isDigit(currTerm.charAt(0))) {
            currTerm.deleteCharAt(0);
        }
        while (currTerm.length() > 0 && !Character.isLetter(currTerm.charAt(currTerm.length() - 1)) && !Character.isDigit(currTerm.charAt(currTerm.length() - 1))) {
            currTerm.deleteCharAt(currTerm.length() - 1);
        }
        return currTerm.toString().toLowerCase();
    }
}
