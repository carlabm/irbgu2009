package edu.bgu.ir2009.auxiliary;

import java.util.*;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 13/01/2010
 * Time: 17:45:44
 */
public class DocumentPostings {
    private final Map<String, Set<Long>> terms = new HashMap<String, Set<Long>>();
    private final String docNo;

    public DocumentPostings() {
        docNo = null;
    }

    public DocumentPostings(String docNo) {
        this.docNo = docNo;
    }

    public void addTerm(String term, long pos) {
        Set<Long> posSet = terms.get(term);
        if (posSet == null) {
            posSet = new LinkedHashSet<Long>();
            terms.put(term, posSet);
        }
        posSet.add(pos);
    }

    public Map<String, Set<Long>> getTerms() {
        return Collections.unmodifiableMap(terms);
    }

    public String getDocNo() {
        return docNo;
    }
}
