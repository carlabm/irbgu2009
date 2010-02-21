package edu.bgu.ir2009;

import edu.bgu.ir2009.auxiliary.*;
import edu.bgu.ir2009.auxiliary.io.*;
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
    private final Configuration config;
    private final Parser parser;
    private final Ranker ranker;
    private final IndexReader<TermData, Object> termIndex;
    private final IndexReader<Map<String, Double>, Object> docVectorsIndex;
    private final IndexReader<Map<String, Set<Long>>, String> nextWordIndex;
    private long searchId = 0;

    public Searcher(Configuration config) throws IOException {
        this.config = config;
        parser = new Parser(config, false);
        ranker = new Ranker(config);
        termIndex = new IndexReader<TermData, Object>(new TermIndexReadStrategy(config));
        docVectorsIndex = new IndexReader<Map<String, Double>, Object>(new DocVectorReader(config));
        nextWordIndex = new IndexReader<Map<String, Set<Long>>, String>(new NextWordIndexReadStrategy(config));
    }

    public TreeSet<RankedDocument> search(String text) {
        try {
            DocumentPostings documentPostings = parser.parse("query-" + String.valueOf(searchId++), text);
            Map<String, Set<Long>> terms = documentPostings.getTerms();
            if (terms.isEmpty()) {
                return new TreeSet<RankedDocument>();
            }
            Set<String> termsSet = terms.keySet();
            Map<String, TermData> termDataMap = retrieveTermData(termsSet);
            Set<String> docs;
            if (!isQuotedQuery(text) || termsSet.size() == 1) {
                docs = merge(termsSet, termDataMap);
                if (docs.size() < 20) {
                    docs.addAll(expandResults(docs.size(), termsSet, termDataMap));
                }
            } else {
                docs = getQuotedDocs(terms);
            }
            if (docs.isEmpty()) {
                return new TreeSet<RankedDocument>();
            }
            GetDocumentVectorsAndTexts documentVectorsAndTexts = new GetDocumentVectorsAndTexts(termDataMap, docs).invoke();
            Map<String, Map<String, Double>> docsVectors = documentVectorsAndTexts.getDocsVectors();
            Map<String, TermNode[]> recomposedDocs = documentVectorsAndTexts.getRecomposedTexts();
            Map<String, List<List<TermNode>>> docsExpandedSpans = getExpandedSpans(docs, recomposedDocs);
            Map<String, Double> queryVector = DocumentVectorsFlushingStrategy.calculateDocumentVector(toTermDFMap(termDataMap), config.getDocumentsCount(), documentPostings);
            return ranker.rank(queryVector, docsVectors, docsExpandedSpans);
        } catch (IOException e) {
            logger.error("Could not retrieve data from index file!!!!!");
            return null;
        }
    }

    private Set<String> expandResults(int currentSize, Set<String> termsSet, Map<String, TermData> termDataMap) {
        Permutation permutation = new Permutation(termsSet);
        Set<String> res = new HashSet<String>();
        for (int i = termsSet.size() - 1; i > -1; i--) {
            List<Set<String>> termPermutations = permutation.getPermutation(i);
            for (Set<String> termPermutation : termPermutations) {
                res.addAll(merge(termPermutation, termDataMap));
            }
            if (currentSize + res.size() > 20) {
                break;
            }
        }
        return res;
    }

    private Set<String> getQuotedDocs(Map<String, Set<Long>> terms) throws IOException {
        Set<String> res = new HashSet<String>();
        TermNode[] recomposedText = TermProximity.recomposeText(terms);
        Map<String, Set<Long>> relevantDocs = nextWordIndex.read(recomposedText[0].getTerm(), recomposedText[1].getTerm());
        if (relevantDocs != null) {
            for (int i = 1; i < recomposedText.length - 1; i++) {
                Map<String, Set<Long>> loopRelevantDocs = nextWordIndex.read(recomposedText[i].getTerm(), recomposedText[i + 1].getTerm());
                if (loopRelevantDocs != null) {
                    relevantDocs.keySet().retainAll(loopRelevantDocs.keySet());
                    Iterator<String> relDocsIterator = relevantDocs.keySet().iterator();
                    while (relDocsIterator.hasNext()) {
                        String docNo = relDocsIterator.next();
                        Set<Long> relevantPostings = relevantDocs.get(docNo);
                        Set<Long> loopPostings = loopRelevantDocs.get(docNo);
                        Iterator<Long> iterator = relevantPostings.iterator();
                        while (iterator.hasNext()) {
                            Long posting = iterator.next();
                            if (!loopPostings.contains(posting + i)) {
                                iterator.remove();
                            }
                        }
                        if (relevantPostings.isEmpty()) {
                            relDocsIterator.remove();
                        }
                    }
                }
            }
            res.addAll(relevantDocs.keySet());
        }
        return res;
    }

    private Map<String, Long> toTermDFMap(Map<String, TermData> termDataMap) {
        Map<String, Long> res = new HashMap<String, Long>();
        for (String term : termDataMap.keySet()) {
            res.put(term, (long) termDataMap.get(term).getPostingsMap().size());
        }
        return res;
    }

    private Map<String, List<List<TermNode>>> getExpandedSpans(Set<String> docs, Map<String, TermNode[]> recomposedDocs) {
        Map<String, List<List<TermNode>>> docsExpandedSpans = new HashMap<String, List<List<TermNode>>>();
        for (String retrievedDoc : docs) {
            docsExpandedSpans.put(retrievedDoc, TermProximity.calculateSpans(recomposedDocs.get(retrievedDoc), config.getDMax()));
        }
        return docsExpandedSpans;
    }

    private Map<String, TermData> retrieveTermData(Set<String> termsSet) throws IOException {
        Map<String, TermData> termDataMap = new HashMap<String, TermData>();
        for (String term : termsSet) {
            TermData termData = termIndex.read(term, null);
            if (termData != null) {
                termDataMap.put(term, termData);
            }
        }
        return termDataMap;
    }

    private boolean isQuotedQuery(String text) {
        text = text.trim();     //todo change to regular expresion
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
//        InMemoryIndex memoryIndex = new InMemoryIndex(configuration, true);
//        long loadStart = System.currentTimeMillis();
//        memoryIndex.load();
//        long loadEnd = System.currentTimeMillis();
//        logger.info("Load took: " + (loadEnd - loadStart) + " ms");
        Searcher searcher = new Searcher(configuration);
        while (true) {
            long start = System.currentTimeMillis();
            Set<RankedDocument> rankedDocumentSet = searcher.search("\"ftkkm sdsiii ddfkll ssikj\"");
            long end = System.currentTimeMillis();
            logger.info("Search took: " + (end - start) + " ms");
            for (RankedDocument docNo : rankedDocumentSet) {
                logger.info(docNo.getDocNum() + " " + docNo.getScore());
            }
        }
    }

    private class GetDocumentVectorsAndTexts {
        private final Map<String, TermData> termDataMap;
        private final Set<String> docs;
        private final Map<String, Map<String, Double>> docsVectors;
        private final Map<String, TermNode[]> recomposedTexts;

        public GetDocumentVectorsAndTexts(Map<String, TermData> termDataMap, Set<String> docs) {
            this.termDataMap = termDataMap;
            this.docs = docs;
            docsVectors = new HashMap<String, Map<String, Double>>();
            recomposedTexts = new HashMap<String, TermNode[]>();
        }

        public Map<String, Map<String, Double>> getDocsVectors() {
            return docsVectors;
        }

        public Map<String, TermNode[]> getRecomposedTexts() {
            return recomposedTexts;
        }

        public GetDocumentVectorsAndTexts invoke() throws IOException {
            for (String retrievedDoc : docs) {
                Map<String, Set<Long>> termsPostings = new HashMap<String, Set<Long>>();
                docsVectors.put(retrievedDoc, docVectorsIndex.read(retrievedDoc, null));
                for (String term : termDataMap.keySet()) {
                    termsPostings.put(term, termDataMap.get(term).getPostingsMap().get(retrievedDoc));
                }
                TermNode[] recomposedText = TermProximity.recomposeText(termsPostings);
                recomposedTexts.put(retrievedDoc, recomposedText);
            }
            return this;
        }
    }
}
