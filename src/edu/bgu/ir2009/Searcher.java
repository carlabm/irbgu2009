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
            ParsedDocument parsedDocument = parser.parse("query-" + String.valueOf(searchId++), text);
            Map<String, Set<Long>> terms = parsedDocument.getTerms();
            if (terms.isEmpty()) {
                return Collections.emptySet();
            }
            Set<String> termsSet = terms.keySet();
            Map<String, TermData> termDataMap = retrieveTermData(termsSet);
            Set<String> docs = merge(termsSet, termDataMap);
            GetDocumentVectorsAndSpans documentVectorsAndSpans = new GetDocumentVectorsAndSpans(termDataMap, docs).invoke();
            Map<String, Map<String, Double>> docsVectors = documentVectorsAndSpans.getDocsVectors();
            Map<String, List<List<TermNode>>> docsExpandedSpans = documentVectorsAndSpans.getDocsExpandedSpans();
            Map<String, TermNode[]> recomposedDocs = documentVectorsAndSpans.getRecomposedTexts();
            Map<String, Double> queryVector = Indexer.calculateDocumentVector(termDataMap, parsedDocument);
            return ranker.rank(queryVector, docsVectors, docsExpandedSpans);
        } catch (IOException e) {
            logger.error("Could not retrieve data from index file!!!!!");
            return null;
        }
    }

    private Map<String, TermData> retrieveTermData(Set<String> termsSet) throws IOException {
        Map<String, TermData> termDataMap = new HashMap<String, TermData>();
        for (String term : termsSet) {
            TermData termData = index.getTermData(term);
            if (termData != null) {
                termDataMap.put(term, termData);
            }
        }
        return termDataMap;
    }

    private boolean isQuotedQuery(String text) {
        text = text.trim();
        int start = text.indexOf('\"');
        int end = text.indexOf('\"', start + 1);
        return (start != -1 & end != -1) && start != end;
    }

    private Set<String> merge(Set<String> queryTermsPostings, Map<String, TermData> termDataMap) {
        Set<String> docs = new HashSet<String>();
        Iterator<String> iterator = queryTermsPostings.iterator();
        if (iterator.hasNext()) {
            TermData termData;
            do {
                String term = iterator.next();
                termData = termDataMap.get(term);
                if (termData != null) {
                    docs.addAll(termData.getPostingsMap().keySet());
                }
            } while (iterator.hasNext() && termData == null);
        }
        while (iterator.hasNext()) {
            TermData termData = termDataMap.get(iterator.next());
            if (termData != null) {
                docs.retainAll(termData.getPostingsMap().keySet());
            }
        }
        return docs;
    }

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        Configuration configuration = new Configuration();
        InMemoryIndex memoryIndex = new InMemoryIndex(configuration);
        long loadStart = System.currentTimeMillis();
        memoryIndex.newLoad();
        long loadEnd = System.currentTimeMillis();
        logger.info("Load took: " + (loadEnd - loadStart) + " ms");
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

    private class GetDocumentVectorsAndSpans {
        private final Map<String, TermData> termDataMap;
        private final Set<String> docs;
        private final Map<String, Map<String, Double>> docsVectors;
        private final Map<String, List<List<TermNode>>> docsExpandedSpans;
        private final Map<String, TermNode[]> recomposedTexts;

        public GetDocumentVectorsAndSpans(Map<String, TermData> termDataMap, Set<String> docs) {
            this.termDataMap = termDataMap;
            this.docs = docs;
            docsVectors = new HashMap<String, Map<String, Double>>();
            docsExpandedSpans = new HashMap<String, List<List<TermNode>>>();
            recomposedTexts = new HashMap<String, TermNode[]>();
        }

        public Map<String, Map<String, Double>> getDocsVectors() {
            return docsVectors;
        }

        public Map<String, List<List<TermNode>>> getDocsExpandedSpans() {
            return docsExpandedSpans;
        }

        public Map<String, TermNode[]> getRecomposedTexts() {
            return recomposedTexts;
        }

        public GetDocumentVectorsAndSpans invoke() throws IOException {
            for (String retrievedDoc : docs) {
                Map<String, Set<Long>> termsPostings = new HashMap<String, Set<Long>>();
                docsVectors.put(retrievedDoc, index.getDocumentVector(retrievedDoc));
                for (String term : termDataMap.keySet()) {
                    termsPostings.put(term, termDataMap.get(term).getPostingsMap().get(retrievedDoc));
                }
                TermNode[] recomposedText = TermProximity.recomposeText(termsPostings);
                recomposedTexts.put(retrievedDoc, recomposedText);
                docsExpandedSpans.put(retrievedDoc, TermProximity.calculateSpans(recomposedText, config.getDMax()));
            }
            return this;
        }
    }
}
