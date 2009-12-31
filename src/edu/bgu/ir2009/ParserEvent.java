package edu.bgu.ir2009;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 31/12/2009
 * Time: 04:09:38
 */
public class ParserEvent {
    private final int parsedDocs;
    private final int totalParsedDocs;

    public ParserEvent(int parsedDocs, int totalParsedDocs) {
        this.parsedDocs = parsedDocs;
        this.totalParsedDocs = totalParsedDocs;
    }

    public int getParsedDocs() {
        return parsedDocs;
    }

    public int getTotalParsedDocs() {
        return totalParsedDocs;
    }
}
