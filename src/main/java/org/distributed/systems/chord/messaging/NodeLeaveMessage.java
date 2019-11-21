package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

import java.io.Serializable;

public class NodeLeaveMessage implements Serializable {

    private ChordNode node;

    public NodeLeaveMessage(ChordNode node) {
        this.node = node;
    }

    public ChordNode getNode() {
        return node;
    }

}
