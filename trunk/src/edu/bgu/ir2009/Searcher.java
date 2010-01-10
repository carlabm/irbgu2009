package edu.bgu.ir2009;

import edu.bgu.ir2009.auxiliary.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * User: Henry Abravanel 310739693
 * Date: 09/01/2010
 * Time: 16:56:00
 */
public class Searcher {
    private final static Logger logger = Logger.getLogger(Searcher.class);
    private final InMemoryIndex index;
    private final Configuration config;
    private final Parser parser;
    private final Ranker ranker;
    private long searchId = 0;

    public Searcher(InMemoryIndex index, Configuration config) {
        this.index = index;
        this.config = config;
        parser = new Parser(config, false);
        ranker = new Ranker(index, config);
    }

    public Set<RankedDocument> search(String text) {
        try {
            ParsedDocument parsedDocument = parser.parse(String.valueOf(searchId++), text);
            Map<String, Set<Long>> terms = parsedDocument.getTerms();
            Map<String, TermData> tmpIndex = new HashMap<String, TermData>();
            Set<String> termsSet = terms.keySet();
            for (String term : termsSet) {  //TODO add LRU to InMemoryIndex
                TermData termData = index.getTermData(term);
                tmpIndex.put(term, termData);
            }
            Set<String> docs = merge(terms, tmpIndex);
            Map<String, Map<String, Double>> docsVectors = new HashMap<String, Map<String, Double>>();
            Map<String, List<List<TermNode>>> docsExpandedSpans = new HashMap<String, List<List<TermNode>>>();
            for (String retrievedDoc : docs) {
                Map<String, Set<Long>> termsPostings = new HashMap<String, Set<Long>>();
                docsVectors.put(retrievedDoc, index.getDocumentVector(retrievedDoc, termsSet));
                for (String term : termsSet) {
                    termsPostings.put(term, tmpIndex.get(term).getPostingsMap().get(retrievedDoc));
                }
                docsExpandedSpans.put(retrievedDoc, TermProximity.calculateSpans(TermProximity.recomposeText(termsPostings), config.getDMax()));
            }
            Map<String, Double> queryVector = Indexer.calculateDocumentVector(tmpIndex, parsedDocument);
            return ranker.rank(queryVector, docsVectors, docsExpandedSpans);
        } catch (IOException e) {
            logger.error("Could not retrieve data from index file!!!!!");
            return null;
        }
    }

    private Set<String> merge(Map<String, Set<Long>> terms, Map<String, TermData> tmpIndex) {
        Set<String> docs = new HashSet<String>();
        Iterator<String> iterator = terms.keySet().iterator();
        if (iterator.hasNext()) {
            String term = iterator.next();
            TermData termData = tmpIndex.get(term);
            if (termData != null) {
                docs.addAll(termData.getPostingsMap().keySet());
            }
        }
        while (iterator.hasNext()) {
            TermData termData = tmpIndex.get(iterator.next());
            if (termData != null) {
                docs.retainAll(termData.getPostingsMap().keySet());
            }
        }
        return docs;
    }

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        Configuration configuration = new Configuration("1/conf.txt");
        InMemoryIndex memoryIndex = new InMemoryIndex(configuration);
        memoryIndex.load();
        Searcher searcher = new Searcher(memoryIndex, configuration);
        while (true) {
            long start = System.currentTimeMillis();
            Set<RankedDocument> rankedDocumentSet = searcher.search("where intellectuals, like the bourgeois");
            long end = System.currentTimeMillis();
            logger.info("Search took: " + (end - start) + " ms");
            for (RankedDocument docNo : rankedDocumentSet) {
                logger.info(docNo.getDocNum() + " " + docNo.getScore());
            }
        }
    }
}
