package edu.bgu.ir2009;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 25/12/2009
 * Time: 17:50:39
 */
public class TermData implements Comparable<TermData> {
    private final String term;
    private long frequency;
    private Map<String, Set<Long>> postingsMap = new HashMap<String, Set<Long>>();

    public TermData(String term) {
        this.term = term;
        this.frequency = 0;
    }

    public void addPosting(String docNo, Set<Long> postings) {
        if (postingsMap.get(docNo) != null) {
            throw new IllegalArgumentException("Postings for term '" + term + "' and docNo '" + docNo + "' already exist!");
        }
        if (postings.size() == 0) {
            throw new IllegalArgumentException("postings set should contain at least one posting");
        }
        frequency += postings.size();
        postingsMap.put(docNo, postings);
    }

    @Override
    public String toString() {
        return "TermData{" +
                "term='" + term + '\'' +
                ", frequency=" + frequency +
                ", postingsMap=" + postingsMap +
                '}';
    }

    public String getSavedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(term).append(':').append(frequency).append('[');
        Iterator<String> docNoIterator = postingsMap.keySet().iterator();
        while (docNoIterator.hasNext()) {
            String docNo = docNoIterator.next();
            builder.append(docNo).append('{');
            Iterator<Long> iterator = postingsMap.get(docNo).iterator();
            while (iterator.hasNext()) {
                Long posting = iterator.next();
                builder.append(posting);
                if (iterator.hasNext()) {
                    builder.append(',');
                }
            }
            builder.append("}");
            if (docNoIterator.hasNext()) {
                builder.append(',');
            }
        }
        builder.append(']');
        return builder.toString();
    }

    public int compareTo(TermData o) {
        return (int) (frequency - o.frequency);
    }
}
