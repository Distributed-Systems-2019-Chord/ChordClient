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
import org.distributed.systems.chord.messaging.FingerTable;
import org.distributed.systems.chord.messaging.KeyValue;
import org.distributed.systems.chord.messaging.NodeJoinMessage;
import org.distributed.systems.chord.messaging.NodeLeaveMessage;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.service.FingerTableService;
import org.distributed.systems.chord.service.StorageService;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.List;

public class Node extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    final ActorRef manager;

    private FingerTableService fingerTableService;
    private StorageService storageService;

    public Node() {
        fingerTableService = new FingerTableService();
        this.storageService = StorageService.getInstance();
        this.manager = Tcp.get(getContext().getSystem()).manager();
    }

    public static Props props(ActorRef manager) {
        return Props.create(Node.class, manager);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log.info("Starting up...     ref: " + getSelf());

        Config config = getContext().getSystem().settings().config();
        final String nodeType = config.getString("myapp.nodeType");
        log.info("DEBUG -- nodetype: " + nodeType);

        this.createMemCacheTCPSocket();

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
                .match(Tcp.Bound.class, msg -> {
                    // This will be called, when the SystemActor bound MemCache interface for the particular node.
                    manager.tell(msg, getSelf());
                    System.out.printf("MemCache Interface for node %s listening to %s \n", getSelf().toString(), msg.localAddress().toString());
                })
                .match(CommandFailed.class, msg -> {
                    getContext().stop(getSelf());
                    System.out.println("Command failed");
                })
                .match(Connected.class, conn -> {
                    System.out.println("MemCache Client connected");
                    manager.tell(conn, getSelf());
                    final ActorRef handler =
                            getContext().actorOf(Props.create(MemCachedHandler.class));
                    getSender().tell(TcpMessage.register(handler), getSelf());
                })
                .match(NodeJoinMessage.class, nodeJoinMessage -> fingerTableService.addSuccessor(nodeJoinMessage.getNode()))
                .match(KeyValue.Put.class, putValueMessage -> {
                    String key = putValueMessage.key;
                    Serializable value = putValueMessage.value;
                    log.info("Put for key, value: " + key + " " + value);
                    this.storageService.put(key, value);
                })
                .match(KeyValue.Get.class, getValueMessage -> {
                    Serializable val = this.storageService.get(getValueMessage.key);
                    getContext().getSender().tell(new KeyValue.Reply(val), ActorRef.noSender());
                })
                .match(FingerTable.Get.class, get -> {
                    List<ChordNode> successors = fingerTableService.chordNodes();
                    getContext().getSender().tell(new FingerTable.Reply(successors), ActorRef.noSender());
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

    private void createMemCacheTCPSocket() {
        final ActorRef tcp = Tcp.get(getContext().getSystem()).manager();
        // Open Socket Address on localhost and random port.
        // TODO: We need to expose this port to the outer world
        InetSocketAddress tcp_socked = new InetSocketAddress("localhost", 0);
        Tcp.Command tcpmsg = TcpMessage.bind(getSelf(), tcp_socked, 100);
        tcp.tell(tcpmsg, getSelf());
    }

}
