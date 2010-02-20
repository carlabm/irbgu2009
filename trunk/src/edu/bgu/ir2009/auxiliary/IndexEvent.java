package edu.bgu.ir2009.auxiliary;

import edu.bgu.ir2009.IndexerV2;

/**
 * User: Henry Abravanel 310739693
 * Date: 31/12/2012
 * Time: 21:47:07
 */
public class IndexEvent {
    private final IndexerV2 indexer;

    public IndexEvent(IndexerV2 indexer) {
        this.indexer = indexer;
    }

    public IndexerV2 getIndexer() {
        return indexer;
    }
}
