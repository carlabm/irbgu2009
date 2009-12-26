package edu.bgu.ir2009;

import java.util.*;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 19:02:24
 */
public class ParsedDocument {
    private Map<String, Set<Long>> terms = new HashMap<String, Set<Long>>();
    private final DocumentReader docReader;

    public ParsedDocument(DocumentReader docReader) {
        this.docReader = docReader;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParsedDocument that = (ParsedDocument) o;

        if (!getDocNo().equals(that.getDocNo())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getDocNo().hashCode();
    }

    public String getOriginalText() {
        return docReader.getText();
    }

    public String getDocNo() {
        return docReader.getDocNo();
    }

    public long getDate() {
        return docReader.getDate();
    }

    public String getByLine() {
        return docReader.getByLine();
    }

    public String getCn() {
        return docReader.getCn();
    }

    public String getIn() {
        return docReader.getIn();
    }

    public String getTp() {
        return docReader.getTp();
    }

    public String getPub() {
        return docReader.getPub();
    }

    public String getPage() {
        return docReader.getPage();
    }
}
