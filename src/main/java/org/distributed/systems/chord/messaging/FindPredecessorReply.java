package org.distributed.systems.chord.messaging;

import akka.actor.ActorRef;
import org.distributed.systems.chord.model.ChordNode;

public class FindPredecessorReply implements Response {

    public static final int UNSET = -1;

    private final ChordNode node;

    private final int index;

    private final ActorRef originalSender;

    public FindPredecessorReply(ChordNode predecessor, int index, ActorRef originalSender) {
        this.node = predecessor;
        this.index = index;
        this.originalSender = originalSender;
    }

    public ChordNode getNode() {
        return node;
    }

    public int getIndex() {
        return index;
    }

    public ActorRef getOriginalSender() {
        return originalSender;
    }
}
