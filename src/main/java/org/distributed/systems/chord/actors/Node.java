package org.distributed.systems.chord.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.distributed.systems.chord.messaging.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private List<Long> fingerTable;
    private Map<String, Serializable> valueStore;

    public Node() {
        this.valueStore = new HashMap<>();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log.info("Starting up...");
    }

    @Override
    public Receive createReceive() {
        log.info("Received a message");

        return receiveBuilder()
                .match(NodeJoinMessage.class, nodeJoinMessage -> {
                    if (fingerTable == null) {
                        fingerTable = new ArrayList<>();
                    }

                    fingerTable.add(nodeJoinMessage.getNodeId());
                })
                .match(PutValueMessage.class, putValueMessage -> {
                    String key = putValueMessage.getKey();
                    Object value = putValueMessage.getValue();
                    log.info("key, value: " + key + " " + value);
                    valueStore.put(putValueMessage.getKey(), putValueMessage.getValue());
                })
                .match(GetValueMessage.class, getValueMessage -> {
                    Serializable val = valueStore.get(getValueMessage.getKey());
                    log.info("The requested value is: " + val);

                    // TODO tell the actor, the main class isn't an actor
//                    getContext().getSender().tell(new GetValueResponseMessage(val), ActorRef.noSender());
                })
                .match(GetFingerTableMessage.class, getFingerTableMessage -> {
                    if (fingerTable != null) {
                        log.info(fingerTable.toString());
                    } else {
                        log.info("Finger table not initialized yet!");
                    }
                })
                .match(NodeLeaveMessage.class, nodeLeaveMessage -> {
                    log.info("Node " + nodeLeaveMessage.getNodeId() + " leaving");
                    fingerTable.remove(nodeLeaveMessage.getNodeId());
                })
                .build();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log.info("Shutting down...");
    }

}
