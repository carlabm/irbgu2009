package edu.bgu.ir2009.auxiliary;

import edu.bgu.ir2009.Indexer;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 30/12/2009
 * Time: 20:59:09
 */
public class DownFacade {
    private final static Logger logger = Logger.getLogger(DownFacade.class);
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

    public void startIndexing(final String docsFolder, final String stopWordsFile, final boolean useStemmer) {
        new Thread(new Runnable() {
            public void run() {
                Indexer indexer = null;
                try {
                    indexer = new Indexer(docsFolder, stopWordsFile, useStemmer);
                } catch (IOException e) {
                    logger.error(e, e);
                }
                UpFacade.getInstance().addIndexBindEvent(indexer);
                try {
                    if (indexer != null) {
                        indexer.start();
                    }
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }).start();
    }
}
