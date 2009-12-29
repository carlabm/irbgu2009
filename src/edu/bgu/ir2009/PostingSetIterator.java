package edu.bgu.ir2009;

import java.util.Iterator;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 28/12/2009
 * Time: 21:03:59
 */
public class PostingSetIterator implements Iterator<Long> {
    private PostingNode curr;

    public PostingSetIterator(PostingNode curr) {
        this.curr = new PostingNode(0L);
        this.curr.setNextNode(curr);
    }

    public boolean hasNext() {
        return curr.getNextNode() != null;
    }

    public Long next() {
        curr = curr.getNextNode();
        return curr.getPosting();
    }

    public Long peekForwardNext() {
        PostingNode forwardNode = curr.getForwardNode();
        if (forwardNode != null) {
            return forwardNode.getPosting();
        }
        return null;
    }

    public void forwardNext() {
        curr = curr.getForwardNode();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
