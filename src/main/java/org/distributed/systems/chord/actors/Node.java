package org.distributed.systems.chord.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import org.distributed.systems.ChordStart;
import org.distributed.systems.chord.messaging.FingerTable;
import org.distributed.systems.chord.messaging.KeyValue;
import org.distributed.systems.chord.messaging.NodeJoinMessage;
import org.distributed.systems.chord.messaging.NodeLeaveMessage;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.repository.FingerRepository;
import org.distributed.systems.chord.repository.NodeRepository;
import org.distributed.systems.chord.service.FingerTableService;
import org.distributed.systems.chord.util.Util;
import org.distributed.systems.chord.util.impl.HashUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Node extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private FingerTableService fingerTableService;
    private Map<String, Serializable> valueStore;
    private HashUtil hashUtil = new HashUtil();
    private NodeRepository nodeRepository = new NodeRepository();

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
            String centralNodeAddress = "akka://ChordNetwork@" + centralEntityAddress + "/user/ChordActor0";
            //"akka://ChordNetwork@127.0.0.1:25521/user/ChordActor0"; //

            ActorSelection centralNode = getContext().actorSelection(centralNodeAddress);
            log.info(getSelf().path() + " Sending message to: " + centralNodeAddress);

            FingerRepository.askForFingerTable(centralNode, new FingerTable.Get(hashUtil.hash(getSelf().toString())), fingerTableService);

        } else if (nodeType.equals("central")) {
            ChordNode central = new ChordNode(hashUtil.hash(Util.getIp(config)), Util.getIp(config), Util.getPort(config));
            fingerTableService.setFingerTable(fingerTableService.initFingerTableCentral(central));
            fingerTableService.setPredecessor(central);
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
                    log.info("send figner table to new node");
//                    List<ChordNode> successors = fingerTableService.chordNodes();
                    getSender().tell(new FingerTable.Reply(
                                    Util.getActorRef(getContext(), fingerTableService.getSuccessor()).anchor(),
                                    Util.getActorRef(getContext(), fingerTableService.getPredecessor()).anchor()),
                            getSelf()
                    );
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

    public ChordNode findSuccessor(long id) {
        // send message
        return null;
    }

    public ChordNode findPredecessor(long id) {
        return null;
    }

    private ChordNode closestPreceedingFinger(long id) {
        for (int i = ChordStart.m; i > 0; i--) {
            List<Long> idsInFingerTable = fingerTableService
                    .getFingers().stream()
                    .map(finger -> finger.getSucc().getId())
                    .collect(Collectors.toList());
            if (idsInFingerTable.contains(id)) {
                //              return

            }
        }
        ChordNode pred = fingerTableService.getPredecessor();
        ActorSelection selectedNode = Util.getActorRef(getContext(), pred);
        return nodeRepository.askForSuccessor(selectedNode); // predecessor.successor
    }
}
