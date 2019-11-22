package org.distributed.systems.chord.model;

import akka.actor.ActorRef;

public class ChordNode {

    private long id;

    public ChordNode(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

}
