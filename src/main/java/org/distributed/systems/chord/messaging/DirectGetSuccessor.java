package org.distributed.systems.chord.messaging;

import akka.actor.ActorRef;

public class DirectGetSuccessor implements Command {
    private final ActorRef joiningNode;

    public DirectGetSuccessor(ActorRef joiningNode) {
        this.joiningNode = joiningNode;
    }

    public ActorRef getOriginalSender() {
        return joiningNode;
    }
}
