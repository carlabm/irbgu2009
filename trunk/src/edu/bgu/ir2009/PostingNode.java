package edu.bgu.ir2009;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 28/12/2009
 * Time: 20:53:43
 */
public class PostingNode {
    private PostingNode nextNode;
    private PostingNode forwardNode;
    private final Long posting;

    public PostingNode(Long posting) {
        if (posting == null || posting < 0L) {
            throw new IllegalArgumentException();
        }
        this.posting = posting;
    }

    public PostingNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(PostingNode nextNode) {
        this.nextNode = nextNode;
    }

    public PostingNode getForwardNode() {
        return forwardNode;
    }

    public void setForwardNode(PostingNode forwardNode) {
        this.forwardNode = forwardNode;
    }

    public Long getPosting() {
        return posting;
    }
}
