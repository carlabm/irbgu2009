package edu.bgu.ir2009.auxiliary;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 14/01/2010
 * Time: 01:27:20
 */
public class Pair<First, Second> {
    private final First first;
    private final Second second;

    public Pair(First first, Second second) {
        this.first = first;
        this.second = second;
    }

    public First getFirst() {
        return first;
    }

    public Second getSecond() {
        return second;
    }
}
