package edu.bgu.ir2009;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 30/12/2009
 * Time: 20:59:09
 */
public class DownFacade {
    private static DownFacade facade;
    private final static Object lock = new Object();

    private DownFacade() {
    }

    public static DownFacade getInstance() {
        if (facade == null) {
            synchronized (lock) {
                if (facade == null) {
                    facade = new DownFacade();
                }
            }
        }
        return facade;
    }

    public void startIndexing(final String docsFolder, final String stopWordsFile, final boolean useStemmer) throws XMLStreamException, FileNotFoundException {
        Indexer indexer = new Indexer(docsFolder, stopWordsFile, useStemmer);
        indexer.start();
    }

}
