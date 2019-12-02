package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

public class StabilizeReply implements Response {
    ChordNode predecessor;

    public StabilizeReply(ChordNode predecessor){
        this.predecessor = predecessor;
    }

    public ChordNode getPredecessor() {
        return this.predecessor;
    }
}
