package edu.bgu.ir2009.auxiliary;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 31/12/2009
 * Time: 04:16:14
 */
public class IndexerEvent {
    private final int indexedDocs;
    private final int totalToIndexDocs;

    public IndexerEvent(int indexedDocs, int totalToIndexDocs) {
        this.indexedDocs = indexedDocs;
        this.totalToIndexDocs = totalToIndexDocs;
    }

    public int getIndexedDocs() {
        return indexedDocs;
    }

    public int getTotalToIndexDocs() {
        return totalToIndexDocs;
    }
}
