package org.distributed.systems.chord.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connected;
import akka.io.TcpMessage;
import com.typesafe.config.Config;
import org.distributed.systems.ChordStart;
import org.distributed.systems.chord.messaging.FingerTable;
import org.distributed.systems.chord.messaging.KeyValue;
import org.distributed.systems.chord.messaging.NodeJoinMessage;
import org.distributed.systems.chord.messaging.NodeLeaveMessage;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.model.finger.Finger;
import org.distributed.systems.chord.model.finger.FingerInterval;
import org.distributed.systems.chord.service.FingerTableService;
import org.distributed.systems.chord.service.StorageService;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.List;
import org.distributed.systems.chord.util.Util;
import org.distributed.systems.chord.util.impl.HashUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Node extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    static final int MEMCACHE_MIN_PORT = 11211;
    static final int MEMCACHE_MAX_PORT = 12235;
    final ActorRef manager;

    private static ChordNode node;
    private FingerTableService fingerTableService;
    private ActorRef storageActorRef;
    private Config config = getContext().getSystem().settings().config();

    public Node() {
        fingerTableService = new FingerTableService();
        this.manager = Tcp.get(getContext().getSystem()).manager();
        this.storageActorRef = getContext().actorOf(Props.create(StorageActor.class));

    }

    public static Props props(ActorRef manager) {
        return Props.create(Node.class, manager);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log.info("Starting up...     ref: " + getSelf());
        final long NODE_ID = new HashUtil().hash(getSelf().path().toSerializationFormat()); // FIXME Should be IP
        System.out.println("Starting up with node_id: " + NODE_ID);

        node = new ChordNode(NODE_ID, Util.getIp(config), Util.getPort(config));
        this.createMemCacheTCPSocket();
        joinNetwork();
    }

    @Override
    public Receive createReceive() {
        log.info("Received a message");

        return receiveBuilder()
                .match(Tcp.Bound.class, msg -> {
                    // This will be called, when the SystemActor bound MemCache interface for the particular node.
                    manager.tell(msg, getSelf());
                    System.out.printf("MemCache Interface for node %s listening to %s \n", getSelf().toString(), msg.localAddress().toString());
                })
                .match(CommandFailed.class, msg -> {
                    System.out.println("Command failed");
                    if (msg.cmd() instanceof Tcp.Bind) {
                        int triedPort = ((Tcp.Bind) msg.cmd()).localAddress().getPort();
                        if (triedPort <= Node.MEMCACHE_MAX_PORT) {
                            System.out.println("Port Binding Failed; Retrying...");
                            createMemCacheTCPSocket(triedPort + 1);
                        } else {
                            System.out.println("Port Binding Failed; Ports for Memcache Interface exhausted");
                            System.out.println("Shutting down...");
                            getContext().stop(getSelf());
                        }
                    }
                })
                .match(Connected.class, conn -> {
                    System.out.println("MemCache Client connected");
                    manager.tell(conn, getSelf());
                    ActorRef memcacheHandler = getContext().actorOf(Props.create(MemcachedActor.class, storageActorRef = this.storageActorRef));
                    getSender().tell(TcpMessage.register(memcacheHandler), getSelf());
                })
                .match(NodeJoinMessage.class, nodeJoinMessage -> {
                    log.info("Msg Received from Node " + getSender().path());
                    //TODO: fingertable atm is not a finger table. Adjust fingertable l8r when we implement fingertable biz logic.
                })
                .match(KeyValue.Put.class, putValueMessage -> {
                    String key = putValueMessage.key;
                    Serializable value = putValueMessage.value;
                    log.info("key, value: " + key + " " + value);
                    this.storageActorRef.forward(putValueMessage, getContext());
                })
                .match(KeyValue.Get.class, getValueMessage -> {
                    this.storageActorRef.forward(getValueMessage, getContext());
                })
                .match(FingerTable.Get.class, get -> {
                    log.info("send figner table to new node");
                    // TODO check if still working

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
                    // TODO implement
                })
                .match(FingerTable.GetSuccessor.class, getSuccessor -> {
                    ChordNode node = fingerTableService.getSuccessor();
                    log.info("FingerTable.GetSuccessor: " + node.getId());
                    getSender().tell(new FingerTable.GetSuccessorReply(node), getSelf());
                })
                .match(FingerTable.GetSuccessorReply.class, getSuccessorReply -> {
                    log.info("FingerTable.GetSuccessorReply: " + getSuccessorReply.getChordNode().getId());

                    // Set my successor based on the answer
                    fingerTableService.setSuccessor(getSuccessorReply.getChordNode());
                })
                .match(FingerTable.GetPredecessor.class, getPredecessor -> {
                    ChordNode node = fingerTableService.getPredecessor();
                    log.info("FingerTable.GetPredecessor: " + node.getId());
                    getSender().tell(new FingerTable.GetPredecessorReply(node), getSelf());
                })
                .match(FingerTable.GetPredecessorReply.class, getPredecessorReply -> {
                    log.info("FingerTable.GetPredecessorReply: " + getPredecessorReply.getChordNode().getId());
                    fingerTableService.setPredecessor(getPredecessorReply.getChordNode());

                    // Now that we have our successor and predecessor set we can generate the finger table
                    generateFingerTable(getCentralNode(getCentralNodeAddress()));
                })
                .match(FingerTable.SetPredecessor.class, setPredecessor -> {
                    // Message some other node sends to me when he is joining the network
                    fingerTableService.setPredecessor(setPredecessor.getNode());
                    log.info("FingerTable.SetPredecessor: " + setPredecessor.getNode().getId());
                })
                .match(FingerTable.GetClosestPrecedingFinger.class, getClosestPrecedingFinger -> {
                    ChordNode closest = closestPrecedingFinger(getClosestPrecedingFinger.getId());
                    log.info("FingerTable.GetClosestPrecedingFinger: " + closest.getId());
                    getSender().tell(new FingerTable.GetClosestPrecedingFingerReply(closest), getSelf());
                })
                .match(FingerTable.GetClosestPrecedingFingerReply.class, getClosestPrecedingFingerReply -> {
                    log.info("FingerTable.GetClosestPrecedingFingerReply: " + getClosestPrecedingFingerReply.getClosestChordNode().getId());

                    // TODO When do we ask for this?
                })
                .match(FingerTable.FindSuccessor.class, findSuccessor -> {
                    ChordNode successor = findSuccessor(findSuccessor.getId());
                    log.info("FingerTable.FindSuccessor: " + successor.getId());
                    getSender().tell(new FingerTable.FindSuccessorReply(successor), getSelf());
                })
                .match(FingerTable.FindSuccessorReply.class, findSuccessorReply -> {
                    log.info("FingerTable.FindSuccessorReply: " + findSuccessorReply.getChordNode());
                    fingerTableService.setSuccessor(findSuccessorReply.getChordNode());

                    System.out.println("Gonna ask for my predecessor!");
                    // FIXME do we need to give my node id to ask for the predecessor?
                    getCentralNode(getCentralNodeAddress()).tell(new FingerTable.GetPredecessor(node.getId()), getSelf());
                    // After this we have set the successor and predecessor and we can build the finger table
                })
                .match(FingerTable.UpdateFinger.class, updateFinger -> {
                    log.info("FingerTable.UpdateFinger " + updateFinger.getFingerTableIndex() + " " + updateFinger.getNodeId());
                    updateFingerTable(updateFinger.getNodeId(), updateFinger.getFingerTableIndex());
                })
                .match(FingerTable.GetFingerTableSuccessor.class, getFingerTableSuccessor -> {
                    ChordNode successor = findSuccessor(getFingerTableSuccessor.getSuccessor());
                    log.info("FingerTable.GetFingerTableSuccessor "
                            + getFingerTableSuccessor.getFingerTableIndex() + " " + getFingerTableSuccessor.getSuccessor());
                    getSender().tell(new FingerTable.GetFingerTableSuccessorReply(getFingerTableSuccessor.getFingerTableIndex()
                            , successor), getSelf());
                })
                .match(FingerTable.GetFingerTableSuccessorReply.class, getFingerTableSuccessorReply -> {
                    log.info("FingerTable.GetFingerTableSuccessorReply fixing finger table "
                            + getFingerTableSuccessorReply.getFingerTableIndex() + " " + getFingerTableSuccessorReply.getSuccessor().getId());

                    fingerTableService.getFingers().get((int) getFingerTableSuccessorReply.getFingerTableIndex())
                            .setSucc(getFingerTableSuccessorReply.getSuccessor());

                    System.out.println("Current finger table after update");
                    fingerTableService.getFingers().forEach(finger -> System.out.println(finger.toString()));

                    if (fingerTableService.getFingers().stream().noneMatch(finger -> finger.getSucc() == null)) {
                        // If the entire finger table is initialized we can update other finger tables to let them know I'm a new node in the network!
                        System.out.println("Updating other finger tables!");

                        updateOthers();
                    }
                })
                .build();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log.info("Shutting down...");
    }

    private void createMemCacheTCPSocket() {
        createMemCacheTCPSocket(Node.MEMCACHE_MIN_PORT);
        // TODO: Environment Var Control?
    }

    private void createMemCacheTCPSocket(int port) {

        final ActorRef tcp = Tcp.get(getContext().getSystem()).manager();
        // TODO: We need to expose this port to the outer world
        // Get possible hostname:
        String hostname = "localhost";

        if (System.getenv("HOSTNAME") != null) {
            hostname = System.getenv("HOSTNAME");
        }


        InetSocketAddress tcp_socked = new InetSocketAddress(hostname, port);
        Tcp.Command tcpmsg = TcpMessage.bind(getSelf(), tcp_socked, 100);
        tcp.tell(tcpmsg, getSelf());
    }

    private ChordNode findSuccessor(long id) {
        ChordNode successor = fingerTableService.getSuccessor();
        ChordNode pred = findPredecessor(id); // FIXME probably has to go trough a message

        // If other node found, ask it for its successor
        if (pred.getId() != successor.getId()) {
            ActorSelection predecessor = Util.getActorRef(getContext(), pred);
            predecessor.tell(FingerTable.GetSuccessor.class, getSelf());
        }

        if (successor == null) {
            return node;
        }
        return successor;
    }

    private ChordNode findPredecessor(long id) {
        ChordNode closest = node;
        //TODO: Don't do rpc call in while check? replace at end of loop maybe?
        while (!(id > closest.getId() && id <= node.getId())) {
            closest = closestPrecedingFinger(id);
            if (node.getId() == closest.getId()) {
                return closest;
            } else {
                Util.getActorRef(getContext(), closest).tell(new FingerTable.GetClosestPrecedingFinger(id), getSelf());
            }
        }
        return closest;
    }

    private ChordNode closestPrecedingFinger(long id) {
        List<Finger> fingers = fingerTableService.getFingers();

        for (int i = ChordStart.m - 1; i > 0; i--) {
//            // Is in interval?
            if (fingers.get(i).getInterval().getStartKey() >= id && id < fingers.get(i).getInterval().getEndKey()) {
                //TODO Check successor != getSelf(); works?
                if (fingers.get(i).getSucc() != node) {
                    // Return closest
                    return fingers.get(i).getSucc();
                }
            }
        }
        // Return self
        return node;
    }

    private void joinNetwork() {
        String nodeType = config.getString("myapp.nodeType");
        
        if (System.getenv("CHORD_NODE_TYPE") != null) {
            nodeType = System.getenv("CHORD_NODE_TYPE");
        }

        log.info("DEBUG -- nodetype: " + nodeType);
        if (nodeType.equals("regular")) {
            ActorSelection centralNode = getCentralNode(getCentralNodeAddress());

            // First step is to find the correct successor
            centralNode.tell(new FingerTable.FindSuccessor(node.getId()), getSelf());

        } else if (nodeType.equals("central")) {
            fingerTableService.setFingerTable(fingerTableService.initFingerTableCentral(node));
            fingerTableService.setSuccessor(node);
            fingerTableService.setPredecessor(node);
        }
    }

    private void generateFingerTable(ActorSelection centralNode) {
        List<Finger> fingerList = new ArrayList<>();

        System.out.println("Building finger table...");

        long first = fingerTableService.startFinger(node.getId(), 1);
        long second = fingerTableService.startFinger(node.getId(), 2);
        FingerInterval firstInterval = new FingerInterval(first, second);
        fingerList.add(new Finger(first, firstInterval, fingerTableService.getSuccessor()));

        for (int i = 1; i < ChordStart.m - 1; i++) { // FIXME finger table is too short..
            long beginFinger = fingerTableService.startFinger(node.getId(), i + 1);
            long nextFinger = fingerTableService.startFinger(node.getId(), i + 2);
            ChordNode currentSuccessor = fingerTableService.getSuccessor(); //getFingers().get(i).getSucc().getId();

            FingerInterval interval = fingerTableService.calcInterval(beginFinger, nextFinger);
            if (nextFinger >= node.getId() && nextFinger < currentSuccessor.getId()) {
                fingerList.add(new Finger(beginFinger, interval, currentSuccessor));
            } else {
                // Ask for successor if not in current interval
                centralNode.tell(new FingerTable.GetFingerTableSuccessor(i, beginFinger), getSelf());

                // Set empty successor will be handled when we get a message back
                fingerList.add(new Finger(beginFinger, interval, null));
            }
        }
        fingerTableService.setFingerTable(new org.distributed.systems.chord.model.finger.FingerTable(fingerList, ChordStart.m));

        System.out.println("THIS IS HOW MY FINGERTABLE LOOKS LIKE: " + "The size is " + fingerList.size() + " and should be " + ChordStart.m);
        fingerList.forEach(finger -> System.out.println(finger.toString()));
    }

    private void updateOthers() {
        for (int i = 1; i < ChordStart.m; i++) {
            ChordNode predecessor = findPredecessor(getFingerWhoseIthFingerMightBeNode(i));

            ActorSelection actorRef = Util.getActorRef(getContext(), predecessor);
            actorRef.tell(new FingerTable.UpdateFinger(node.getId(), i), getSelf());
        }
    }

    private long getFingerWhoseIthFingerMightBeNode(int i) {
        return (long) (node.getId() - Math.pow(2, (i - 1)));
    }

    private void updateFingerTable(long nodeId, int index) {
        if (nodeId > node.getId() && nodeId <= fingerTableService.getSuccessor().getId()) {
            ActorSelection actorRef = Util.getActorRef(getContext(), fingerTableService.getPredecessor());
            actorRef.tell(new FingerTable.UpdateFinger(node.getId(), index), getSelf());
        }
    }

    private ActorSelection getCentralNode(String centralNodeAddress) {
        return getContext().actorSelection(centralNodeAddress);
    }

    private String getCentralNodeAddress() {
        String centralEntityAddress = config.getString("myapp.centralEntityAddress");
        
        if (System.getenv("CHORD_CENTRAL_NODE") != null) {
            centralEntityAddress = System.getenv("CHORD_CENTRAL_NODE");
        }

        return "akka://ChordNetwork@" + centralEntityAddress + "/user/ChordActor0";
    }
}
