package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

public class SetPredecessor implements Command {

    private final ChordNode node;

    public SetPredecessor(ChordNode node) {
        this.node = node;
    }

    public ChordNode getNode() {
        return node;
    }
}
