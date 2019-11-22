package org.distributed.systems;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.distributed.systems.chord.actors.Node;
import org.distributed.systems.chord.messaging.FingerTable;
import org.distributed.systems.chord.messaging.KeyValue;
import org.distributed.systems.chord.messaging.NodeJoinMessage;
import org.distributed.systems.chord.messaging.NodeLeaveMessage;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.util.IHashUtil;
import org.distributed.systems.chord.util.impl.HashUtil;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static akka.pattern.Patterns.ask;

public class ChordStart {

    private static final int STANDARD_TIME_OUT = 1000;

    public static void main(String[] args) {
        IHashUtil hashUtil = new HashUtil();

        // Create actor system
        ActorSystem system = ActorSystem.create("ChordNetwork"); // Setup actor system

        // Create start node
        ChordNode startNode = new ChordNode(0L);
        final ActorRef node = system.actorOf(Props.create(Node.class), "ChordActor0");

        String hashId = hashUtil.hash(String.valueOf(startNode.getId()));
        String hashKey = hashUtil.hash(String.valueOf(startNode.getId()));

        // Create (tell) messages
        NodeJoinMessage joinMessage = new NodeJoinMessage(startNode);
        NodeLeaveMessage leaveMessage = new NodeLeaveMessage(startNode);

        FingerTable.Get getFingerTable = new FingerTable.Get(hashId);

        KeyValue.Put putValueMessage = new KeyValue.Put(hashKey, "This is some kind of test value");

        // Send messages to the node
        askForFingerTable(node, getFingerTable);

        // Node is joining..
        node.tell(joinMessage, ActorRef.noSender());
        askForFingerTable(node, getFingerTable);

        // Add en retrieve value
        node.tell(putValueMessage, ActorRef.noSender());
        askForValue(node, new KeyValue.Get(hashKey));

        // Node is leaving after sometime...
        node.tell(leaveMessage, ActorRef.noSender());
        askForFingerTable(node, getFingerTable);

//        system.stop(node); // Quit node

//        system.terminate(); // Terminate application
    }

    private static void askForFingerTable(ActorRef node, FingerTable.Get getFingerTable) {
        // Prepare
        CompletableFuture<Object> fingerTableRequest = ask(node, getFingerTable, Duration.ofMillis(STANDARD_TIME_OUT)).toCompletableFuture();

        // Send
        CompletableFuture<FingerTable.Reply> transformed =
                CompletableFuture.allOf(fingerTableRequest)
                        .thenApply(v -> (FingerTable.Reply) fingerTableRequest.join());

        // Handle response
        transformed.whenComplete((getFingerTableMessage, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            } else {
                System.out.println("Response for get finger table: " +
                        getFingerTableMessage.successors.stream()
                                .map(ChordNode::getId)
                                .collect(Collectors.toList())
                                .toString());
            }
        });
    }

    private static void askForValue(ActorRef node, KeyValue.Get getValue) {
        // Prepare
        CompletableFuture<Object> valueRequest = ask(node, getValue, Duration.ofMillis(STANDARD_TIME_OUT)).toCompletableFuture();

        // Send
        CompletableFuture<KeyValue.Reply> transformed =
                CompletableFuture.allOf(valueRequest)
                        .thenApply(v -> (KeyValue.Reply) valueRequest.join());

        // Handle response
        transformed.whenComplete((getValueRequest, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            } else {
                System.out.println("Response for get value: " + getValueRequest.value.toString());
            }
        });
    }
}
