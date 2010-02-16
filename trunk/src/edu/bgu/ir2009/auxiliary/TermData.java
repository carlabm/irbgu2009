package edu.bgu.ir2009.auxiliary;

import java.util.*;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 25/12/2009
 * Time: 17:50:39
 */
public class TermData implements Comparable<TermData> {
    private final String term;
    private final Object lock = new Object();
    private Map<String, Set<Long>> postingsMap = Collections.synchronizedMap(new HashMap<String, Set<Long>>());
    private long frequency;
    private double idf = -1.0;

    public TermData(String term) {
        this.term = term;
        this.frequency = 0;
    }

    public TermData(String term, String serialized) {
        this.term = term;
        int start = serialized.indexOf(':') + 1;
        int end = serialized.indexOf(':', start);
        frequency = Long.parseLong(serialized.substring(start, end));
        start = end + 1;
        end = serialized.indexOf('[', start);
        idf = Double.parseDouble(serialized.substring(start, end));
        start = end + 1;
        end = serialized.indexOf(']');
        while (start < end) {
            int currDocDataEnd = serialized.indexOf('{', start);
            String currDocNo = serialized.substring(start, currDocDataEnd);
            start = currDocDataEnd + 1;
            currDocDataEnd = serialized.indexOf('}', start);
            Set<Long> postings = getPostings(serialized, start, currDocDataEnd);
            postingsMap.put(currDocNo, postings);
            start = currDocDataEnd + 2;
        }
    }

    public void addPosting(String docNo, Set<Long> postings) {
        if (postings.size() == 0) {
            throw new IllegalArgumentException("postings set should contain at least one posting");
        }
        synchronized (lock) {
            frequency += postings.size();
            postingsMap.put(docNo, postings);
        }
    }

    public String getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return "TermData{" +
                "term='" + term + '\'' +
                ", frequency=" + frequency +
                ", postingsMap=" + postingsMap +
                '}';
    }

    /* public String getSavedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(term).append(':').append(frequency).append(':').append(idf).append('[');
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
    }*/

    public String getSerializedPostings() {
        StringBuilder builder = new StringBuilder();
        for (String docNo : postingsMap.keySet()) {
            builder.append(docNo).append('{');
            for (Long posting : postingsMap.get(docNo)) {
                builder.append(posting).append(',');
            }
            builder.append('}').append(',');
        }
        return builder.toString();
    }

    public int compareTo(TermData o) {
        return (int) (frequency - o.frequency);
    }

    public void setTotalDocs(long totalDocs) {
        idf = Math.log10((double) totalDocs / postingsMap.size());
    }

    public double getIdf() {
        return idf;
    }

    public Map<String, Set<Long>> getPostingsMap() {
        return Collections.unmodifiableMap(postingsMap);
    }

    private Set<Long> getPostings(String data, int start, int end) {
        Set<Long> postings = new LinkedHashSet<Long>();
        while (start < end) {
            int currEnd = data.indexOf(',', start);
            if (currEnd == -1 || currEnd > end) {
                currEnd = end;
            }
            postings.add(Long.parseLong(data.substring(start, currEnd)));
            start = currEnd + 1;
        }
        return postings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TermData termData = (TermData) o;

        if (frequency != termData.frequency) return false;
        if (Double.compare(termData.idf, idf) != 0) return false;
        if (!postingsMap.equals(termData.postingsMap)) return false;
        if (!term.equals(termData.term)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = term.hashCode();
        result = 31 * result + postingsMap.hashCode();
        result = 31 * result + (int) (frequency ^ (frequency >>> 32));
        temp = idf != +0.0d ? Double.doubleToLongBits(idf) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
