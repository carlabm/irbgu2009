package edu.bgu.ir2009.auxiliary;

import java.util.*;

/**
 * User: Henry Abravanel 310739693
 * Date: 09/01/2010
 * Time: 19:33:27
 */
public class TermProximity {
    public static final int D_MAX = 10;
    private final double LAMBDA;
    private final double GAMMA;

    public static TermNode[] recomposeText(Map<String, Set<Long>> termsPostings) {
        Set<TermNode> resSet = new TreeSet<TermNode>();
        for (String term : termsPostings.keySet()) {
            for (Long position : termsPostings.get(term)) {
                resSet.add(new TermNode(term, position));
            }
        }
        TermNode[] res = new TermNode[resSet.size()];
        resSet.toArray(res);
        return res;
    }

    public static List<List<TermNode>> calculateSpans(TermNode[] recomposedText) {
        List<List<TermNode>> res = new LinkedList<List<TermNode>>();
        List<TermNode> currSpan = new LinkedList<TermNode>();
        res.add(currSpan);
        for (int i = 0; i < recomposedText.length - 1; i++) {
            TermNode next = recomposedText[i + 1];
            TermNode curr = recomposedText[i];
            if (next.getPosition() - curr.getPosition() > D_MAX) {
                currSpan.add(curr);
                currSpan = new LinkedList<TermNode>();
                res.add(currSpan);
            } else {
                if (next.getTerm().equals(curr.getTerm())) {
                    currSpan.add(curr);
                    currSpan = new LinkedList<TermNode>();
                    res.add(currSpan);
                } else {
                    int index;
                    if ((index = currSpan.indexOf(new TermNode(next.getTerm(), -1L))) != -1) {
                        TermNode termNode = currSpan.get(index);
                        long prevDistance = curr.getPosition() - termNode.getPosition();
                        long nextDistance = next.getPosition() - curr.getPosition();
                        if (prevDistance > nextDistance) {
                            currSpan = new LinkedList<TermNode>();
                            res.add(currSpan);
                            currSpan.add(curr);
                        } else {
                            currSpan.add(curr);
                            currSpan = new LinkedList<TermNode>();
                            res.add(currSpan);
                        }
                    } else {
                        currSpan.add(curr);
                    }
                }
            }
        }
        TermNode lastTerm = recomposedText[recomposedText.length - 1];
        List<TermNode> lastSpan = res.get(res.size() - 1);
        if (lastSpan.contains(lastTerm)) {
            res.add(Arrays.asList(lastTerm));
        } else {
            lastSpan.add(lastTerm);
        }
        return res;
    }


    public static long calculateSpanWidth(List<TermNode> span) {
        long res = D_MAX;
        if (span.size() != 1) {
            res = span.get(span.size() - 1).getPosition() - span.get(0).getPosition() + 1L;
        }
        return res;
    }

    public TermProximity(Configuration config) {
        LAMBDA = config.getLambda();
        GAMMA = config.getGamma();
    }

    public double calculateTermWeight(String term, List<TermNode> span) {
        return Math.pow(span.size(), GAMMA) / Math.pow(calculateSpanWidth(span), LAMBDA);
    }

    public double calculateRelevanceContribution(String term, List<List<TermNode>> spans) {
        double res = 0.0;
        for (List<TermNode> span : spans) {
            res += calculateTermWeight(term, span);
        }
        return res;
    }
}
