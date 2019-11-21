package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

import java.io.Serializable;

public class NodeJoinMessage implements Serializable {

    private ChordNode node;

    public NodeJoinMessage(ChordNode node) {
        this.node = node;
    }

    public ChordNode getNode() {
        return node;
    }
}
