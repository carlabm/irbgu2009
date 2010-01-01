package edu.bgu.ir2009.auxiliary;

import java.util.*;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 19:02:24
 */
public class ParsedDocument {
    private final Map<String, Set<Long>> terms = new HashMap<String, Set<Long>>();
    private final String docNo;
    private final long date;
    private final String byLine;
    private final String cn;
    private final String in;
    private final String tp;
    private final String pub;
    private final String page;
    private final String text;

    public ParsedDocument(UnParsedDocument unParsedDoc) {
        docNo = unParsedDoc.getDocNo();
        date = unParsedDoc.getDate();
        byLine = unParsedDoc.getByLine();
        cn = unParsedDoc.getCn();
        in = unParsedDoc.getIn();
        tp = unParsedDoc.getTp();
        pub = unParsedDoc.getPub();
        page = unParsedDoc.getPage();
        text = unParsedDoc.getText();
    }

    public ParsedDocument(String serialized) {
        int start = 0;
        int end = serialized.indexOf('|');
        docNo = serialized.substring(start, end);
        start = end + 1;
        end = serialized.indexOf('|', start);
        date = Long.parseLong(serialized.substring(start, end));
        start = end + 1;
        end = serialized.indexOf('|', start);
        byLine = serialized.substring(start, end);
        start = end + 1;
        end = serialized.indexOf('|', start);
        cn = serialized.substring(start, end);
        start = end + 1;
        end = serialized.indexOf('|', start);
        in = serialized.substring(start, end);
        start = end + 1;
        end = serialized.indexOf('|', start);
        tp = serialized.substring(start, end);
        start = end + 1;
        end = serialized.indexOf('|', start);
        pub = serialized.substring(start, end);
        start = end + 1;
        end = serialized.indexOf('|', start);
        page = serialized.substring(start, end);
        start = end + 1;
        text = serialized.substring(start);
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

    public String serialize() {
        StringBuilder builder = new StringBuilder();
        return builder.append(docNo.replace('|', ' ')).append('|')
                .append(date).append('|')
                .append(byLine.replace('|', ' ')).append('|')
                .append(cn.replace('|', ' ')).append('|')
                .append(in.replace('|', ' ')).append('|')
                .append(tp.replace('|', ' ')).append('|')
                .append(pub.replace('|', ' ')).append('|')
                .append(page.replace('|', ' ')).append('|')
                .append(text).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParsedDocument that = (ParsedDocument) o;

        if (!docNo.equals(that.docNo)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return docNo.hashCode();
    }

    public String getDocNo() {
        return docNo;
    }

    public long getDate() {
        return date;
    }

    public String getByLine() {
        return byLine;
    }

    public String getCn() {
        return cn;
    }

    public String getIn() {
        return in;
    }

    public String getTp() {
        return tp;
    }

    public String getPub() {
        return pub;
    }

    public String getPage() {
        return page;
    }

    public String getText() {
        return text;
    }
}
