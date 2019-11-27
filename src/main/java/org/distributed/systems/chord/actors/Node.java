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
import org.distributed.systems.chord.model.finger.Finger;
import org.distributed.systems.chord.repository.NodeRepository;
import org.distributed.systems.chord.service.FingerTableService;
import org.distributed.systems.chord.util.Util;
import org.distributed.systems.chord.util.impl.HashUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Node extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private static ChordNode node;
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
        final long NODE_ID = hashUtil.hash(getSelf().path().toSerializationFormat()); // FIXME Should be IP
        System.out.println("Starting up with node_id: " + NODE_ID);

        Config config = getContext().getSystem().settings().config();
        final String nodeType = config.getString("myapp.nodeType");
        log.info("DEBUG -- nodetype: " + nodeType);
        node = new ChordNode(NODE_ID, Util.getIp(config), Util.getPort(config));

        if (nodeType.equals("regular")) {
            final String centralEntityAddress = config.getString("myapp.centralEntityAddress");
            String centralNodeAddress = "akka://ChordNetwork@" + centralEntityAddress + "/user/ChordActor0";
            ActorSelection centralNode = getContext().actorSelection(centralNodeAddress);
            log.info(getSelf().path() + " Sending message to: " + centralNodeAddress);

            // FingerTable central
//            FingerRepository.askForFingerTable(centralNode, new FingerTable.Get(hashUtil.hash(getSelf().toString())), fingerTableService);

            // Extract useful information from fingerTable

            // Ask nearest actor for finger table
//            Long id = hashUtil.hash(getSelf().toString());
            nodeRepository.askForSuccessor(centralNode, node.getId()).whenComplete((getSuccessorReply, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                System.out.println("I found my successor! " + getSuccessorReply.getChordNode().getId());
                fingerTableService.setSuccessor(getSuccessorReply.getChordNode());
            });

        } else if (nodeType.equals("central")) {
            fingerTableService.setFingerTable(fingerTableService.initFingerTableCentral(node));
            fingerTableService.setPredecessor(node);
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
                                    fingerTableService.getFingers().stream()
                                            .map(Finger::getSucc)
                                            .collect(Collectors.toList()),
                                    fingerTableService.getPredecessor()),
                            getSelf()
                    );
                })
                .match(NodeLeaveMessage.class, nodeLeaveMessage -> {
//                    log.info("Node " + nodeLeaveMessage.getNode().getId() + " leaving");
//                    fingerTableService.removeSuccessor(nodeLeaveMessage.getNode());
                })
                .match(FingerTable.GetSuccessor.class, getSuccessor -> {
                    ChordNode node = closestPrecedingFinger(getSuccessor.getId());
                    getSender().tell(new FingerTable.GetSuccessorReply(node), getSelf());
                })
                .build();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log.info("Shutting down...");
    }

    public ChordNode findSuccessor(long id) {
        ChordNode successor = fingerTableService.getSuccessor();
        ChordNode pred = findPredecessor(id);

        if(pred.getId() == successor.getId()){
            successor = nodeRepository.askForSuccessor(getContext(), pred).join().getChordNode();
        }

        if (successor == null){
            return node;
        }
        return successor;
    }

    public ChordNode findPredecessor(long id) {
        ChordNode node = null;
        while (!(id > this.node.getId() && id < fingerTableService.getSuccessor().getId() - 1)) {
            return nodeRepository.askForPredecessor(getContext(), closestPrecedingFinger(id)).join().getChordNode();
        }

        return node;
    }

    private ChordNode closestPrecedingFinger(long id) {
        List<Finger> fingers = fingerTableService.getFingers();

        for (int i = ChordStart.m - 1; i > 0; i--) {
//            // Is in interval?
            if (fingers.get(i).getInterval().getStartKey() > id && id < fingers.get(i).getInterval().getEndKey()) {
                //TODO Check successor != getSelf();
                // Return closest
                return fingers.get(i).getSucc();
            }
        }

        // Return self
        return node;
    }


    public void initFingerTable() {
        // Loop over chord network (find_successor() -> closest_preceding_finger()) and find the successor of me + 1 (finger 1)
        ChordNode finger = findSuccessor(node.getId());

        // Get for first finger the predecessor, this will be our predecessor (ask)
        CompletableFuture<FingerTable.GetPredecessorReply> predecessorMessage = nodeRepository.askForPredecessor(getContext(), finger);

        predecessorMessage.whenComplete((predecessorRequestReply, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            } else {
                // Set my predecessor
                fingerTableService.setPredecessor(predecessorRequestReply.getChordNode());
            }
        });
        // Tell successor that I am his new predecessor
//

        for (int i = 1; i < ChordStart.m - 1; i++) {
            //
//            if()
            //
//            else
        }
    }
}
