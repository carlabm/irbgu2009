package edu.bgu.ir2009.auxiliary;

/**
 * User: Henry Abravanel 310739693
 * Date: 09/01/2010
 * Time: 18:23:22
 */
public class RankedDocument implements Comparable<RankedDocument> {
    private final String docNum;
    private final double score;

    public RankedDocument(String docNum, double score) {
        this.docNum = docNum;
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RankedDocument that = (RankedDocument) o;

        if (docNum != null ? !docNum.equals(that.docNum) : that.docNum != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return docNum != null ? docNum.hashCode() : 0;
    }

    public int compareTo(RankedDocument o) {
        double diffScore = score - o.score;
        int res = 0;
        if (diffScore < 0.0) {
            res = 1;
        } else {
            if (diffScore > 0.0) {
                res = -1;
            }
        }
        return res;
    }

    public String getDocNum() {
        return docNum;
    }

    public double getScore() {
        return score;
    }
}
