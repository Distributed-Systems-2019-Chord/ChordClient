package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

public class FixFingersReply implements Response {
    private ChordNode successor;
    private int index;

    public FixFingersReply(ChordNode successor, int index){

    }
    public ChordNode getSuccessor(){
        return this.successor;
    }

    public int getIndex() {
        return index;
    }
}
