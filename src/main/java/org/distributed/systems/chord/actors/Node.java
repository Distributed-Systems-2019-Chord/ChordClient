package org.distributed.systems.chord.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import org.distributed.systems.chord.messaging.FingerTable;
import org.distributed.systems.chord.messaging.KeyValue;
import org.distributed.systems.chord.messaging.NodeJoinMessage;
import org.distributed.systems.chord.messaging.NodeLeaveMessage;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.service.FingerTableService;
import org.distributed.systems.chord.util.impl.HashUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Node extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private ChordNode chordNode;
    private FingerTableService fingerTableService;
    private Map<String, Serializable> valueStore;
    private HashUtil hashUtil = new HashUtil();

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
            // FIXME whe shouldn't hardcode this ChordActor ID...
            String centralNodeAddress = "akka://ChordNetwork@127.0.0.1:25521/user/ChordActor0#-961619862"; //"akka://ChordNetwork@" + centralEntityAddress + "/user/ChordActor";

            ActorSelection centralNode = getContext().actorSelection(centralNodeAddress);
            log.info(getSelf().path() + " Sending message to: " + centralNodeAddress);

            fingerTableService.askForFingerTable(centralNode,
                    new FingerTable.Get(
                            hashUtil.hash(
                                    getSelf().toString()
                            )));

        } else if (nodeType.equals("central")) {
            this.fingerTableService.setSuccessor(getSelf());
            this.fingerTableService.setPredecessor(getSelf());
        }
    }

    @Override
    public Receive createReceive() {
        log.info("Received a message");

        return receiveBuilder()
                .match(NodeJoinMessage.class, nodeJoinMessage -> {
                    log.info("Msg Received from Node " + getSender().path());
                    //TODO: fingertable atm is not a finger table. Adjust fingertable l8r when we implement fingertable biz logic.

                })
                .match(KeyValue.Put.class, putValueMessage -> {
                    String key = putValueMessage.key;
                    Serializable value = putValueMessage.value;
                    log.info("key, value: " + key + " " + value);
                })
                .match(KeyValue.Put.class, putValueMessage -> {
                    String key = putValueMessage.key;
                    Serializable value = putValueMessage.value;
                    log.info("Put for key, value: " + key + " " + value);
                    valueStore.put(key, value);
                })
                .match(KeyValue.Get.class, getValueMessage -> {
                    Serializable val = valueStore.get(getValueMessage.key);
                    getContext().getSender().tell(new KeyValue.Reply(val), ActorRef.noSender());
                })
                .match(FingerTable.Get.class, get -> {
//                    List<ChordNode> successors = fingerTableService.chordNodes();
                    getSender().tell(new FingerTable.Reply(fingerTableService.getSuccessor(), fingerTableService.getPredecessor()), getSelf());
                })
                .match(NodeLeaveMessage.class, nodeLeaveMessage -> {
//                    log.info("Node " + nodeLeaveMessage.getNode().getId() + " leaving");
//                    fingerTableService.removeSuccessor(nodeLeaveMessage.getNode());
                })
                .build();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log.info("Shutting down...");
    }

}
