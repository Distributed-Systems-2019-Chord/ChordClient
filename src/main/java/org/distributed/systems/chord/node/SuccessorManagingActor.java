package org.distributed.systems.chord.node;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.FI;
import org.distributed.systems.chord.messaging.NodeJoinMessage;
import org.distributed.systems.chord.model.Successor;

public class SuccessorManagingActor extends AbstractActor {

    private Successor successor;

    static Props props() {
        return Props.create(Successor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(NodeJoinMessage.class, handleJoiningNode()).build();
    }

    private FI.UnitApply<NodeJoinMessage> handleJoiningNode() {
        // TODO
        return null;
    }

}
