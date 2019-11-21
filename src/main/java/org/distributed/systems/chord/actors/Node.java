package org.distributed.systems.chord.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import org.distributed.systems.chord.messaging.*;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.service.FingerTableService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private FingerTableService fingerTableService;
    private Map<String, Serializable> valueStore;

    public Node() {
        this.valueStore = new HashMap<>();
        fingerTableService = new FingerTableService();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log.info("Starting up...     ref: " + getSelf());

        Config config = getContext().getSystem().settings().config();
        final String nodeType = config.getString("myapp.nodeType");
        log.info("DEBUG -- nodetype: " + nodeType);

        if (nodeType.equals("regular")) {
            final String centralEntityAddress = config.getString("myapp.centralEntityAddress");
            String centralNodeAddress = "akka://ChordNetwork@" + centralEntityAddress + "/user/a";
            log.info("Sending message to: " + centralNodeAddress);
            ActorSelection selection = getContext().actorSelection(centralNodeAddress);

//            test call
            selection.tell("newNode", getSelf());

//          TODO get fingertable from central entity
//            CompletableFuture<Object> future = getContext().ask(selection,
//                    new fingerTableActor.getFingerTable(line), 1000).toCompletableFuture();
        }
    }

    @Override
    public Receive createReceive() {
        log.info("Received a message");

        return receiveBuilder()
                .match(NodeJoinMessage.class, nodeJoinMessage -> fingerTableService.addSuccessor(nodeJoinMessage.getNode()))
                .match(PutValueMessage.class, putValueMessage -> {
                    String key = putValueMessage.getKey();
                    Serializable value = putValueMessage.getValue();
                    log.info("key, value: " + key + " " + value);
                    valueStore.put(key, value);
                })
                .match(GetValueMessage.class, getValueMessage -> {
                    Serializable val = valueStore.get(getValueMessage.getKey());
                    log.info("The requested value is: " + val);

                    // TODO tell the actor, the main class isn't an actor though so it won't work
//                    getContext().getSender().tell(new GetValueResponseMessage(val), ActorRef.noSender());
                })
                .match(GetFingerTableMessage.class, getFingerTableMessage -> {
                    List<ChordNode> successors = fingerTableService.chordNodes();
                    log.info(successors.toString());
                    // TODO tell the actor, the main class isn't an actor though so it won't work
//                    getContext().getSender().tell(new FingerTableResponseMessage(successors), ActorRef.noSender());
                })
                .match(NodeLeaveMessage.class, nodeLeaveMessage -> {
                    log.info("Node " + nodeLeaveMessage.getNode().getId() + " leaving");
                    fingerTableService.removeSuccessor(nodeLeaveMessage.getNode());
                })
                .build();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log.info("Shutting down...");
    }

}
