package edu.bgu.ir2009.auxiliary;

import java.util.*;

/**
 * User: Henry Abravanel 310739693
 * Date: 09/01/2010
 * Time: 19:33:27
 */
public class TermProximity {
    public static final int D_MAX = 10;

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

    public static List<Set<TermNode>> calculateSpans(TermNode[] recomposedText) {
        List<Set<TermNode>> res = new LinkedList<Set<TermNode>>();
        TreeSet<TermNode> currSpan = new TreeSet<TermNode>();
        for (int i = 0; i < recomposedText.length; i++) {
            if (i < recomposedText.length - 1) {
                if (recomposedText[i + 1].getPosition() - recomposedText[i].getPosition() > D_MAX) {
                    currSpan.add(recomposedText[i]);
                    res.add(currSpan);
                    currSpan = new TreeSet<TermNode>();
                } else {
                    if (recomposedText[i + 1].getTerm().equals(recomposedText[i].getTerm())) {
                        currSpan.add(recomposedText[i]);
                        res.add(currSpan);
                        currSpan = new TreeSet<TermNode>();
                    } else {
                        //TODO last case
                    }
                }
            }

        }
        return res;
    }

    public static void main(String[] args) {
        Map<String, Set<Long>> termsPostings = new HashMap<String, Set<Long>>();
        LinkedHashSet<Long> longLinkedHashSet = new LinkedHashSet<Long>();
        longLinkedHashSet.add(0L);
        longLinkedHashSet.add(3L);
        longLinkedHashSet.add(6L);
        termsPostings.put("i", longLinkedHashSet);
        longLinkedHashSet = new LinkedHashSet<Long>();
        longLinkedHashSet.add(1L);
        longLinkedHashSet.add(4L);
        longLinkedHashSet.add(7L);
        termsPostings.put("am", longLinkedHashSet);
        longLinkedHashSet = new LinkedHashSet<Long>();
        longLinkedHashSet.add(2L);
        longLinkedHashSet.add(5L);
        termsPostings.put("henry", longLinkedHashSet);
        Set<TermNode> termNodes = recomposeText(termsPostings);
        int i = 0;
    }
}
