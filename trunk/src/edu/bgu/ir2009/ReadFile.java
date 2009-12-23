package edu.bgu.ir2009;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 23/12/2009
 * Time: 15:08:03
 */
public class ReadFile {
    private final String dirName;

    private BlockingQueue<DocumentReader> docQueue = new LinkedBlockingQueue<DocumentReader>();

    public ReadFile(String dirName) {
        this.dirName = dirName;
    }

    public void start(String fileName) {

    }
}
