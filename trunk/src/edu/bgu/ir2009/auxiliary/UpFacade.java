package edu.bgu.ir2009.auxiliary;

import edu.bgu.ir2009.Indexer;

import java.util.Observable;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 31/12/2009
 * Time: 03:22:22
 */
public class UpFacade extends Observable {
    private static UpFacade facade;
    private final static Object lock = new Object();

    private UpFacade() {
    }

    public static UpFacade getInstance() {
        if (facade == null) {
            synchronized (lock) {
                if (facade == null) {
                    facade = new UpFacade();
                }
            }
        }
        return facade;
    }

    public void addReaderEvent(int filesRead, int totalFiles) {
        setChanged();
        notifyObservers(new ReaderEvent(filesRead, totalFiles));
    }

    public void addParserEvent(int parsedDocs, int totalParsedDocs) {
        setChanged();
        notifyObservers(new ParserEvent(parsedDocs, totalParsedDocs));
    }

    public void addIndexerEvent(int indexedDocs, int totalToIndexDocs) {
        setChanged();
        notifyObservers(new IndexerEvent(indexedDocs, totalToIndexDocs));
    }

    public void addIndexSavingEvent(int savedTerm, int totalTerms) {
        setChanged();
        notifyObservers(new SavingEvent(savedTerm, totalTerms));
    }

    public void addDocumentsSavingEvent(int savedDocs, int totalDocs) {
        setChanged();
        notifyObservers(new DocumentsSavingEvent(savedDocs, totalDocs));
    }

    public void addIndexBindEvent(Indexer indexEvent) {
        setChanged();
        notifyObservers(new IndexEvent(indexEvent));
    }
}
