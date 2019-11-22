package org.distributed.systems.chord.model;

import akka.actor.ActorRef;

import java.io.Serializable;

public class ChordNode implements Serializable {

    private long id;

    public ChordNode(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

}
