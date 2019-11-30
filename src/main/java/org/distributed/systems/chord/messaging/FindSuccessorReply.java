package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

public class FindSuccessorReply implements Response {

    private final ChordNode node;

    public FindSuccessorReply(ChordNode successor) {
        this.node = successor;
    }

    public ChordNode getNode() {
        return node;
    }
}
