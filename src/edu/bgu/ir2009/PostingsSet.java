package edu.bgu.ir2009;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * User: Henry Abravanel 310739693 henrya@bgu.ac.il
 * Date: 28/12/2009
 * Time: 20:48:32
 */
public class PostingsSet {
    private Set<Long> backendSet = new HashSet<Long>();
    private PostingNode start = null;
    private PostingNode end = null;
    private int size = 0;
    private boolean locked = false;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(long o) {
        return backendSet.contains(o);
    }

    public Iterator<Long> iterator() {
        if (!locked) {
            throw new IllegalStateException("set must be locked to get its iterator");
        }
        return new PostingSetIterator(start);
    }

    public boolean add(long posting) {
        if (locked) {
            throw new IllegalStateException("cannot add elements to an locked set");
        }
        if (backendSet.contains(posting)) {
            return false;
        }
        if (end != null && end.getPosting() > posting) {
            throw new IllegalArgumentException("posting must be bigger then the biggest posting in the set");
        }
        size++;
        PostingNode newPostingNode = new PostingNode(posting);
        if (isEmpty()) {
            start = newPostingNode;
            end = newPostingNode;
        } else {
            end.setNextNode(newPostingNode);
            end = newPostingNode;
        }
        return backendSet.add(posting);
    }

    public void lock() {
        locked = true;
        int jumpSize = (int) Math.sqrt(size);
        if (jumpSize > 0) {
            PostingNode curr = start;
            PostingNode toUpdate = start;
            while (curr != null) {
                for (int i = 0; i < jumpSize && curr != null; i++) {
                    curr = curr.getNextNode();
                }
                toUpdate.setForwardNode(curr);
            }
        }
    }
}
