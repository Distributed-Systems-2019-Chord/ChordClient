package org.distributed.systems.chord.messaging;

import org.distributed.systems.chord.model.ChordNode;

public class Notify implements Command {

    ChordNode node;

    public Notify(ChordNode node){
        this.node = node;
    }

    public ChordNode getNode(){
        return this.node;
    }
}
