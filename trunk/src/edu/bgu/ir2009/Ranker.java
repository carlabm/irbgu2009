package edu.bgu.ir2009;

import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.InMemoryIndex;
import edu.bgu.ir2009.auxiliary.RankedDocument;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * User: Henry Abravanel 310739693
 * Date: 09/01/2010
 * Time: 18:18:36
 */
public class Ranker {
    private final InMemoryIndex index;
    private final Configuration config;

    public Ranker(InMemoryIndex index, Configuration config) {
        this.index = index;
        this.config = config;
    }

    public Set<RankedDocument> rank(Map<String, Double> queryVector, Map<String, Map<String, Double>> toBeRankedVectors) {
        Set<RankedDocument> res = new TreeSet<RankedDocument>();
        for (String docNo : toBeRankedVectors.keySet()) {
            double score = 0.0;
            Map<String, Double> currDocVector = toBeRankedVectors.get(docNo);
            for (String queryTerm : queryVector.keySet()) {
                Double termWeight = currDocVector.get(queryTerm);
                if (termWeight != null) {
                    score += termWeight * queryVector.get(queryTerm);
                }
            }
            res.add(new RankedDocument(docNo, score));
        }
        return res;
    }

}
