package edu.bgu.ir2009.auxiliary;

import edu.bgu.ir2009.Indexer;

/**
 * User: Henry Abravanel 310739693
 * Date: 31/12/2012
 * Time: 21:47:07
 */
public class IndexEvent {
    private final Indexer indexer;

    public IndexEvent(Indexer indexer) {
        this.indexer = indexer;
    }

    public Indexer getIndexer() {
        return indexer;
    }
}
