package edu.bgu.ir2009;

import edu.bgu.ir2009.auxiliary.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693
 * Date: 09/01/2010
 * Time: 16:56:00
 */
public class Searcher {
    private final static Logger logger = Logger.getLogger(Searcher.class);
    private final InMemoryIndex index;
    private final Parser parser;
    private final Ranker ranker;
    private long searchId = 0;

    public Searcher(InMemoryIndex index, Configuration config) {
        this.index = index;
        parser = new Parser(config, false);
        ranker = new Ranker(index, config);
    }

    public Set<RankedDocument> search(String text) {
        ParsedDocument parsedDocument = parser.parse(String.valueOf(searchId++), text);
        Map<String, Set<Long>> terms = parsedDocument.getTerms();
        Map<String, TermData> tmpIndex = new HashMap<String, TermData>();
        Set<String> docs = new HashSet<String>();
        try {
            for (String term : terms.keySet()) {
                TermData termData = index.getTermData(term);
                tmpIndex.put(term, termData);
            }
        } catch (IOException e) {
            logger.error("Could not retrieve data from index file!!!!!");
            return null;
        }
        Map<String, Double> queryVector = Indexer.calculateDocumentVector(tmpIndex, parsedDocument);
        boolean firstLoop = true;
        for (String term : terms.keySet()) {
            TermData termData = tmpIndex.get(term);
            if (termData != null) {
                Set<String> docsForTerm = termData.getPostingsMap().keySet();
                if (firstLoop) {
                    firstLoop = false;
                    docs.addAll(docsForTerm);
                } else {
                    docs.retainAll(docsForTerm);
                }
            }
        }
        Map<String, Map<String, Double>> docsVectors = new HashMap<String, Map<String, Double>>();
        try {
            for (String retrievedDoc : docs) {
                docsVectors.put(retrievedDoc, index.getDocumentVector(retrievedDoc));
            }
        } catch (IOException e) {
            logger.error("Could not retrieve data from index file!!!!!");
            return null;
        }
        Set<RankedDocument> documentSet = ranker.rank(queryVector, docsVectors);
        return documentSet;
    }

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        Configuration configuration = new Configuration("10/conf.txt");
        InMemoryIndex memoryIndex = new InMemoryIndex(configuration);
        memoryIndex.load();
        Searcher searcher = new Searcher(memoryIndex, configuration);
        Set<RankedDocument> rankedDocumentSet = searcher.search("outside consultants");
        for (RankedDocument docNo : rankedDocumentSet) {
            logger.info(docNo.getDocNum() + " " + docNo.getScore());
        }
    }
}
