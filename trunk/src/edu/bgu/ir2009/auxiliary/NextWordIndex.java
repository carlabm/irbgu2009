package edu.bgu.ir2009.auxiliary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 13/01/2010
 * Time: 02:02:04
 */
public class NextWordIndex {
    private final Map<String, Map<String, Map<String, Set<Long>>>> index = new HashMap<String, Map<String, Map<String, Set<Long>>>>();

    public void addWordPair(String doc, String first, String second, Long pos) {
        Map<String, Map<String, Set<Long>>> nextWordMap = index.get(first);
        if (nextWordMap == null) {
            nextWordMap = new HashMap<String, Map<String, Set<Long>>>();
            index.put(first, nextWordMap);
        }
        Map<String, Set<Long>> postingsMap = nextWordMap.get(second);
        if (postingsMap == null) {
            postingsMap = new HashMap<String, Set<Long>>();
            nextWordMap.put(second, postingsMap);
        }
        Set<Long> docPostings = postingsMap.get(doc);
        if (docPostings == null) {
            docPostings = new HashSet<Long>();
            postingsMap.put(doc, docPostings);
        }
        docPostings.add(pos);
    }
}
