package edu.bgu.ir2009.auxiliary;

/**
 * User: Henry Abravanel 310739693
 * Date: 09/01/2010
 * Time: 19:37:02
 */
public class TermNode implements Comparable<TermNode> {
    private final String term;
    private final Long position;

    public TermNode(String term, Long position) {
        this.term = term;
        this.position = position;
    }

    public String getTerm() {
        return term;
    }

    public Long getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TermNode termNode = (TermNode) o;

        if (!term.equals(termNode.term)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return term != null ? term.hashCode() : 0;
    }

    public int compareTo(TermNode o) {
        return (int) (position - o.position);
    }
}
