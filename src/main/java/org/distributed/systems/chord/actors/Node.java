package org.distributed.systems.chord.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connected;
import akka.io.TcpMessage;
import com.typesafe.config.Config;
import org.distributed.systems.ChordStart;
import org.distributed.systems.chord.messaging.*;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.model.finger.Finger;
import org.distributed.systems.chord.model.finger.FingerInterval;
import org.distributed.systems.chord.service.FingerTableService;
import org.distributed.systems.chord.util.Util;
import org.distributed.systems.chord.util.impl.HashUtil;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.List;

public class Node extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    static final int MEMCACHE_MIN_PORT = 11211;
    static final int MEMCACHE_MAX_PORT = 12235;
    final ActorRef manager;

    private int generateFingerCount = 1;
    private static ChordNode node;
    private FingerTableService fingerTableService;
    private ActorRef storageActorRef;
    private Config config = getContext().getSystem().settings().config();
    private boolean memCacheEnabled = false;

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
        long envVal;
        if (System.getenv("node.id") == null) {
            envVal = new HashUtil().hash(getSelf().path().toSerializationFormat()); // FIXME Should be IP
        } else {
            envVal = Long.parseLong(System.getenv("node.id"));
        }
        final long NODE_ID = envVal;
        System.out.println("Starting up with node_id: " + NODE_ID);

        node = new ChordNode(NODE_ID, Util.getIp(config), Util.getPort(config));
        if (memCacheEnabled) {
            this.createMemCacheTCPSocket();
        }
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
                .match(KeyValue.Put.class, putValueMessage -> {
                    String key = putValueMessage.key;
                    Serializable value = putValueMessage.value;
                    log.info("key, value: " + key + " " + value);
                    this.storageActorRef.forward(putValueMessage, getContext());
                })
                .match(KeyValue.Get.class, getValueMessage -> {
                    this.storageActorRef.forward(getValueMessage, getContext());
                })


                // FIXME From here
                .match(FindSuccessor.class, findSuccessor -> {
                    log.info("FindSuccessor");
                    if (getSender().equals(getSelf())) {
//                        log.info("I was the sender, I will solve it myself");
//                        log.info("I am in init table phase");


                        // I will set my successor
                        fingerTableService.getFingers().get(generateFingerCount).setSucc(fingerTableService.getSuccessor());

                        // Check if we need more finger entries
                        if (isFingerTableNotComplete()) {
                            loopAndFixFingers();
                        } else {
                            printFingerTable();
                            updateOthers();
                        }
                        return;
                    }

                    // Find predecessor --> in reply send FindSuccessorReply
                    tellFindPredecessor(findSuccessor.getId(), getSender());

                })
                .match(FindSuccessorReply.class, findSuccessorReply -> {
//                    log.info("FindSuccessorReply");
                    // Reply on the DirectGetSuccessor message (that is called after the findPredecessor)
//                    fingerTableService.setSuccessor(findSuccessorReply.getNode());

                    //FIXME doesn't work yet... Update its successor
//                    Util.getActorRef(getContext(), findSuccessorReply.getNode()).tell(new SetSuccessor(node), getSelf());

                    // The successor and predecessor of this node are now known
                    initialiseFirstFinger(findSuccessorReply.getNode());
                    generateFingerTable(getCentralNode(getCentralNodeAddress()));
                })
                .match(FindPredecessor.class, findPredecessor -> {
//                    log.info("FindPredecessor");
                    tellFindPredecessor(findPredecessor.getId(), findPredecessor.getIndex(), getSelf());
                })
                .match(FindPredecessorReply.class, findPredecessorReply -> {
//                    log.info("FindPredecessorReply");
                    // Init finger table phase -> set first
                    if (fingerTableService.getFingers().isEmpty()) {
//                        log.info("Init finger table phase ");
                        fingerTableService.setPredecessor(findPredecessorReply.getNode());
                        Util.getActorRef(getContext(), findPredecessorReply.getNode()).tell(new SetPredecessor(node), getSelf());
                        Util.getActorRef(getContext(), findPredecessorReply.getNode()).tell(new DirectGetSuccessor(getSelf()), getSelf());
                    }

                    // Init finger table phase -> finger table is not complete
                    else if (isFingerTableNotComplete()) {
                        loopAndFixFingers();
                    }

                    // FIXME!!!!!
                    // Update others phase. This message should be send by the original sender
                    else if (findPredecessorReply.getIndex() != FindPredecessorReply.UNSET) { // is not unset
//                        log.info("UpdateFinger is this the joining node?");
                        ActorSelection toUpdatePredRef = Util.getActorRef(getContext(), findPredecessorReply.getNode());
                        findPredecessorReply.getOriginalSender().tell(new YouShouldUpdateThisNode(toUpdatePredRef, findPredecessorReply.getIndex()), getSelf());
//                        toUpdatePredRef.tell(new UpdateFinger(findPredecessorReply.getIndex(), node), getSelf());
                    }
                    // We are in the chain of asking for predecessors
                    // I shouldn't send this message to myself
                    else if (getSelf() != findPredecessorReply.getOriginalSender()) {
//                        log.info("chain of asking for predecessor?");
                        findPredecessorReply.getOriginalSender().tell(new FindPredecessorReply(findPredecessorReply.getNode(), findPredecessorReply.getIndex(), findPredecessorReply.getOriginalSender()), getSelf());
                    } else {
                        log.info("I'm just gonna tell some node he should update its finger table");
                        Util.getActorRef(getContext(), findPredecessorReply.getNode()).tell(new UpdateFinger(findPredecessorReply.getIndex(), node), getSelf());
                        log.info("My finger table should be complete, and I'm the node that initiated a call.");
                        printFingerTable();

                        log.info("What is this? " + findPredecessorReply.getIndex() + " " + findPredecessorReply.getNode().getId());
                    }

                })
                .match(SetPredecessor.class, setPredecessor -> {
                    log.info("I have a new predecessor");
                    fingerTableService.setPredecessor(setPredecessor.getNode());
                })
                .match(DirectGetSuccessor.class, directGetSuccessor -> {
                    log.info("DirectGetSuccessor");
                    // Tell joining node that this is the successor he is searching for
                    directGetSuccessor.getOriginalSender().tell(new FindSuccessorReply(fingerTableService.getSuccessor()), getSelf());
                })
                .match(YouShouldUpdateThisNode.class, youShouldUpdateThisNode -> {
                    // FIXME Broken somehow?
                    getCentralNode(getCentralNodeAddress()).tell(new UpdateFinger(youShouldUpdateThisNode.getIndex(), node), getSelf());
//                    youShouldUpdateThisNode.getToUpdatePredRef().tell(new UpdateFinger(youShouldUpdateThisNode.getIndex(), node), getSelf());
                })
                .match(UpdateFinger.class, updateFinger -> {
                    log.info("UpdateFinger");
                    updateFingerTable(updateFinger.getNode(), updateFinger.getIndex());
                })
                .build();
    }

    private void joinNetwork() {
        final String nodeType = config.getString("myapp.nodeType");
        log.info("DEBUG -- nodetype: " + nodeType);
        if (nodeType.equals("regular")) {
            ActorSelection centralNode = getCentralNode(getCentralNodeAddress());

            // First step is to find the correct successor
            centralNode.tell(new FindSuccessor(node.getId()), getSelf());

        } else if (nodeType.equals("central")) {
            fingerTableService.setFingerTable(fingerTableService.initFingerTableCentral(node));
            fingerTableService.setSuccessor(node);
            fingerTableService.setPredecessor(node);
        }
    }

    private void initialiseFirstFinger(ChordNode successor) {
        long first = fingerTableService.startFinger(node.getId(), 1);
        long second = fingerTableService.startFinger(node.getId(), 2);
        FingerInterval firstInterval = new FingerInterval(first, second);
        fingerTableService.getFingers().add(new Finger(first, firstInterval, successor));
    }


    private void generateFingerTable(ActorSelection centralNode) {
        long beginFinger = fingerTableService.startFinger(node.getId(), generateFingerCount + 1);
        long nextFinger = fingerTableService.startFinger(node.getId(), generateFingerCount + 2);
        ChordNode currentSuccessor = fingerTableService.getSuccessor(); //getFingers().get(i).getSucc().getId();

        FingerInterval interval = fingerTableService.calcInterval(beginFinger, nextFinger);
        if (nextFinger >= node.getId() && nextFinger < currentSuccessor.getId()) {
            fingerTableService.getFingers().add(new Finger(beginFinger, interval, currentSuccessor));
//            log.info("Sending to my self: " + getSelf().toString());

            getSelf().tell(new FindSuccessor(beginFinger), getSelf());
        } else {
            // Set empty successor will be handled when we get a message back
            fingerTableService.getFingers().add(new Finger(beginFinger, interval, null));

            // Ask for successor if not in current interval
            centralNode.tell(new FindSuccessor(beginFinger), getSelf());
        }
    }

    private void loopAndFixFingers() {
//        log.info("Init finger table phase finger table is not complete");
        fingerTableService.getFingers().get(generateFingerCount)
                .setSucc(fingerTableService.getSuccessor());

        generateFingerCount++;
        if (generateFingerCount < ChordStart.m) {
            generateFingerTable(getCentralNode(getCentralNodeAddress()));
        } else {
//            log.info("Update other nodes finger table should now be complete");
            printFingerTable();

            updateOthers();
        }
    }

    private boolean between(long beginKey, long endKey, long id) {
        if (beginKey > endKey) {
            return !(id <= beginKey && id > endKey);
        } else if (endKey > beginKey) {
            return (id > beginKey && id <= endKey);
        } else {
            return true; // There is just one node
        }
    }

    private void tellFindPredecessor(long id, ActorRef originalSender) {
        tellFindPredecessor(id, FindPredecessorReply.UNSET, originalSender); // index not set
    }

    private void tellFindPredecessor(long id, int index, ActorRef originalSender) {
        // If not in my interval
        if (!between(node.getId(), fingerTableService.getSuccessor().getId(), id)) {
            // Find closest preceding finger in my finger table
            ChordNode predecessor = closestPrecedingFinger(id);
            ActorSelection closestPredNode = Util.getActorRef(getContext(), predecessor);

            // Tell him to return his predecessor
            closestPredNode.tell(new FindPredecessor(id, index, originalSender), getSelf()); //TODO check probably also broken..
//            closestPredNode.forward(new FindPredecessor(id, index), getContext());
        } else {
            // If I'm the node return my predecessor to the original sender
            originalSender.tell(new FindPredecessorReply(fingerTableService.getPredecessor(), index, getSender()), getSelf());
        }
    }

    private ChordNode closestPrecedingFinger(long id) {
        List<Finger> fingers = fingerTableService.getFingers();
        ChordNode currSucc = null;
        for (int i = ChordStart.m - 1; i > 0; i--) {
//            // Is in interval?
            if (between(fingers.get(i).getInterval().getStartKey(), fingers.get(i).getInterval().getEndKey() - 1, id)) {
//            if (fingers.get(i).getInterval().getStartKey() >= id && id < fingers.get(i).getInterval().getEndKey()) {
                currSucc = fingers.get(i).getSucc();
//            if (node.getId() < currSucc.getId() && currSucc.getId() < id) {
                //TODO Check successor != getSelf(); works?
                if (currSucc != node) {
                    // Return closest
                    return currSucc;
                }
            }
        }
        // Return self
        return node;
    }

    private void updateOthers() {
//        log.info("I'm calling this and I should be the joining node: " + getSelf());

        for (int i = 1; i < ChordStart.m; i++) {
            tellFindPredecessor(getFingerWhoseIthFingerMightBeNode(i), i, getSelf());
        }
    }

    private long getFingerWhoseIthFingerMightBeNode(int i) {
        return (long) (node.getId() - Math.pow(2, (i - 1)));
    }

    private void updateFingerTable(ChordNode inNode, int index) {
        if (between(node.getId(), fingerTableService.getFingers().get(index).getSucc().getId(), inNode.getId())) {
            // Update my finger table
            fingerTableService.getFingers().get(index).setSucc(inNode);

            log.info("My finger table has been updated");

            ActorSelection actorRef = Util.getActorRef(getContext(), fingerTableService.getPredecessor());
            actorRef.tell(new UpdateFinger(index, node), getSelf());
        } else {
            log.info("My finger table has not been updated");
        }
        printFingerTable();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log.info("Shutting down...");
    }

    // Util methods
    private boolean isFingerTableNotComplete() {
        return fingerTableService.getFingers().stream().anyMatch(finger -> finger.getSucc() == null);
    }

    private void createMemCacheTCPSocket() {
        createMemCacheTCPSocket(Node.MEMCACHE_MIN_PORT);
        // TODO: Environment Var Control?
    }

    private void createMemCacheTCPSocket(int port) {

        final ActorRef tcp = Tcp.get(getContext().getSystem()).manager();
        // TODO: We need to expose this port to the outer world
        InetSocketAddress tcp_socked = new InetSocketAddress("localhost", port);
        Tcp.Command tcpmsg = TcpMessage.bind(getSelf(), tcp_socked, 100);
        tcp.tell(tcpmsg, getSelf());
    }

    private void printFingerTable() {
        log.info(fingerTableService.toString());
    }

    private ActorSelection getCentralNode(String centralNodeAddress) {
        return getContext().actorSelection(centralNodeAddress);
    }

    private String getCentralNodeAddress() {
        final String centralEntityAddress = config.getString("myapp.centralEntityAddress");
        return "akka://ChordNetwork@" + centralEntityAddress + "/user/ChordActor";
    }
}
