package edu.bgu.ir2009.auxiliary;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 31/12/2009
 * Time: 04:40:13
 */
public class SavingEvent {
    private final int savedTerm;
    private final int totalTerms;

    public SavingEvent(int savedTerm, int totalTerms) {
        this.savedTerm = savedTerm;
        this.totalTerms = totalTerms;
    }

    public int getSavedTerm() {
        return savedTerm;
    }

    public int getTotalTerms() {
        return totalTerms;
    }
}
