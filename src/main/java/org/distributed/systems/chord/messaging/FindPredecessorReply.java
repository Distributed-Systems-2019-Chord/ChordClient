package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

public class FindPredecessorReply implements Response {

    private final ChordNode node;

    private final int index;

    public FindPredecessorReply(ChordNode predecessor, int index) {
        this.node = predecessor;
        this.index = index;
    }

    public ChordNode getNode() {
        return node;
    }

    public int getIndex() {
        return index;
    }
}
