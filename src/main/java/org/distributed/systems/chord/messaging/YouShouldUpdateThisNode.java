package org.distributed.systems.chord.messaging;

import akka.actor.ActorSelection;

public class YouShouldUpdateThisNode implements Command {

    private final ActorSelection toUpdatePredRef;

    private final int index;

    public YouShouldUpdateThisNode(ActorSelection toUpdatePredRef, int index) {
        this.toUpdatePredRef = toUpdatePredRef;
        this.index = index;
    }

    public ActorSelection getToUpdatePredRef() {
        return toUpdatePredRef;
    }

    public int getIndex() {
        return index;
    }
}
