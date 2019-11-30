package org.distributed.systems.chord.messaging;

import akka.actor.ActorRef;

public class FindPredecessor implements Command {
    private final long id;

    private final int index;

    private final ActorRef originalSender;

    public FindPredecessor(long id, int index, ActorRef originalSender) {
        this.id = id;
        this.index = index;
        this.originalSender = originalSender;
    }

    public long getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public ActorRef getOriginalSender() {
        return originalSender;
    }
}
