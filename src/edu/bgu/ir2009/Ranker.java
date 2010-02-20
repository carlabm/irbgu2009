package edu.bgu.ir2009;

import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.RankedDocument;
import edu.bgu.ir2009.auxiliary.TermNode;
import edu.bgu.ir2009.auxiliary.TermProximity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * User: Henry Abravanel 310739693
 * Date: 09/01/2010
 * Time: 18:18:36
 */
public class Ranker {
    private final Configuration config;
    private final TermProximity proximity;

    public Ranker(Configuration config) {
        this.config = config;
        proximity = new TermProximity(config);
    }

    public Set<RankedDocument> rank(Map<String, Double> queryVector, Map<String, Map<String, Double>> toBeRankedVectors, Map<String, List<List<TermNode>>> docsExpandedSpans) {
        Set<RankedDocument> res = new TreeSet<RankedDocument>();
        for (String docNo : toBeRankedVectors.keySet()) {
            double score = 0.0;
            Map<String, Double> currDocVector = toBeRankedVectors.get(docNo);
            for (String queryTerm : queryVector.keySet()) {
                Double termWeight = currDocVector.get(queryTerm);
                if (termWeight != null) {
                    score += termWeight * queryVector.get(queryTerm) * proximity.calculateRelevanceContribution(queryTerm, docsExpandedSpans.get(docNo));
                }
            }
            res.add(new RankedDocument(docNo, score));
        }
        return res;
    }

}
