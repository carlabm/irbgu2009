package edu.bgu.ir2009.auxiliary;

import java.util.*;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 19:02:24
 */
public class ParsedDocument {
    private final UnParsedDocument unParsedDoc;
    private final Map<String, Set<Long>> terms = new HashMap<String, Set<Long>>();

    public ParsedDocument(UnParsedDocument unParsedDoc) {
        this.unParsedDoc = unParsedDoc;
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

    public long getDate() {
        return unParsedDoc.getDate();
    }

    public String getIn() {
        return unParsedDoc.getIn();
    }

    public String getTp() {
        return unParsedDoc.getTp();
    }

    public String getPub() {
        return unParsedDoc.getPub();
    }

    public String getByLine() {
        return unParsedDoc.getByLine();
    }

    public String getCn() {
        return unParsedDoc.getCn();
    }

    public String getDocNo() {
        return unParsedDoc.getDocNo();
    }

    public String getPage() {
        return unParsedDoc.getPage();
    }

    public String getText() {
        return unParsedDoc.getText();
    }
}
