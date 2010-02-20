package edu.bgu.ir2009.auxiliary.io;

import edu.bgu.ir2009.auxiliary.Configuration;
import edu.bgu.ir2009.auxiliary.TermData;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 17/02/2010
 * Time: 22:17:24
 */
public class TermIndexReadStrategy implements IndexReadStrategy<TermData, Object> {
    private final Configuration config;

    public TermIndexReadStrategy(Configuration config) {
        this.config = config;
    }

    public String getIndexFileName() {
        return config.getIndexFileName();
    }

    public String getRefFileName() {
        return config.getIndexReferenceFileName();
    }

    public TermData processLine(String line, Object o) {
        int start = 0;
        int end = line.indexOf(':', start);
        TermData res = new TermData(line.substring(start, end));
        start = end + 1;
        while (start < line.length()) {
            int currDocDataEnd = line.indexOf('{', start);
            String currDocNo = line.substring(start, currDocDataEnd);
            start = currDocDataEnd + 1;
            currDocDataEnd = line.indexOf('}', start);
            res.addPosting(currDocNo, getPostings(line, start, currDocDataEnd));
            start = currDocDataEnd + 2;
        }
        return res;
    }

    private Set<Long> getPostings(String line, int start, int end) {
        Set<Long> postings = new LinkedHashSet<Long>();
        while (start < end) {
            int currEnd = line.indexOf(',', start);
            postings.add(Long.parseLong(line.substring(start, currEnd)));
            start = currEnd + 1;
        }
        return postings;
    }
}
