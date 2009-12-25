package edu.bgu.ir2009;

import org.apache.log4j.BasicConfigurator;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 19:00:01
 */
public class Parser {
    private final Set<String> stopWordsSet = new HashSet<String>();
    private Set<ParsedDocument> parsedDocs = new LinkedHashSet<ParsedDocument>();
    private boolean useStemmer;
    private Stemmer stemmer;

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
        Parser parser = new Parser(config);
        final ReadFile readFile = new ReadFile("FT933");
        new Thread(new Runnable() {
            public void run() {
                try {
                    readFile.start("FT933_1");
                    readFile.setReaderFinished();
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        DocumentReader nextDoc;
        while ((nextDoc = readFile.getNextDocument()) != null) {
            ParsedDocument pd = parser.parse(nextDoc);
            parser.parsedDocs.add(pd);
        }
        int j = 0;
    }

    public ParsedDocument parse(DocumentReader docReader) {
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
