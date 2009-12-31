package edu.bgu.ir2009.auxiliary;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 31/12/2009
 * Time: 03:27:50
 */
public class ReaderEvent {
    private final int filesRead;
    private final int totalFiles;

    public ReaderEvent(int filesRead, int totalFiles) {
        this.filesRead = filesRead;
        this.totalFiles = totalFiles;
    }

    public int getFilesRead() {
        return filesRead;
    }

    public int getTotalFiles() {
        return totalFiles;
    }
}
