package edu.bgu.ir2009.auxiliary;

/**
 * User: Henry Abravanel 310739693
 * Date: 31/12/2012
 * Time: 21:02:38
 */
public class DocumentsSavingEvent {
    private final int savedDocs;
    private final int totalDocs;

    public DocumentsSavingEvent(int savedDocs, int totalDocs) {
        this.savedDocs = savedDocs;
        this.totalDocs = totalDocs;
    }

    public int getSavedDocs() {
        return savedDocs;
    }

    public int getTotalDocs() {
        return totalDocs;
    }
}
