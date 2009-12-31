package edu.bgu.ir2009.auxiliary;

import java.text.ParseException;
import java.util.*;

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

    public TermData(String term, String serialized) throws ParseException {
        this.term = term;
        extractFrequency(serialized);
        validate(serialized);
    }

    private void validate(String originalSerialized) throws ParseException {
        long calculatedFreq = 0;
        for (Set<Long> postings : postingsMap.values()) {
            calculatedFreq += postings.size();
        }
        if (calculatedFreq != frequency) {
            throw new ParseException("parsing term data for " + term + " failed! Original serialized: " + originalSerialized, 0);
        }
    }

    private void extractFrequency(String serialized) {
        int startIndex = serialized.indexOf(':') + 1;
        int endIndex = serialized.indexOf('[');
        frequency = Long.parseLong(serialized.substring(startIndex, endIndex));
        extractPostings(serialized.substring(endIndex + 1));
    }

    private void extractPostings(String serialized) {
        int endIndex;
        while ((endIndex = serialized.indexOf('|')) != -1 || (endIndex = serialized.indexOf(']')) != -1) {
            String working = serialized.substring(0, endIndex);
            extractPosting(working);
            serialized = serialized.substring(endIndex + 1);
        }
    }

    private void extractPosting(String working) {
        int docNameIndex = working.indexOf('{');
        String docNo = working.substring(0, docNameIndex);
        working = working.substring(docNameIndex + 1);
        Set<Long> postings = new LinkedHashSet<Long>();
        int endIndex;
        while ((endIndex = working.indexOf(',')) != -1 || (endIndex = working.indexOf('}')) != -1) {
            postings.add(Long.parseLong(working.substring(0, endIndex)));
            working = working.substring(endIndex + 1);
        }
        postingsMap.put(docNo, postings);
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
                builder.append('|');
            }
        }
        builder.append(']');
        return builder.toString();
    }

    public int compareTo(TermData o) {
        return (int) (frequency - o.frequency);
    }
}
