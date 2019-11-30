package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

public class UpdateFinger implements Command {
    private final int index;

    private final ChordNode node;

    public UpdateFinger(int index, ChordNode node) {
        this.index = index;
        this.node = node;
    }

    public int getIndex() {
        return index;
    }

    public ChordNode getNode() {
        return node;
    }
}
