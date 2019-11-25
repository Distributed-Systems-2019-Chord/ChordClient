package org.distributed.systems;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import org.distributed.systems.chord.actors.Node;
import org.distributed.systems.chord.messaging.KeyValue;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.util.IHashUtil;
import org.distributed.systems.chord.util.impl.HashUtil;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static akka.pattern.Patterns.ask;

public class ChordStart {

    public static final int STANDARD_TIME_OUT = 5000;

    public static void main(String[] args) {
        IHashUtil hashUtil = new HashUtil();

        // Create actor system
        ActorSystem system = ActorSystem.create("ChordNetwork"); // Setup actor system

        // Create start node
        ChordNode startNode = new ChordNode(0L);

        Config config = system.settings().config();
        final String nodeType = config.getString("myapp.nodeType");

        if (nodeType.equals("central")) {
            final ActorRef node = system.actorOf(Props.create(Node.class), "ChordActor0");
        }
        else {
            final ActorRef node = system.actorOf(Props.create(Node.class));
        }


//        String hashId = hashUtil.hash(String.valueOf(startNode.getId()));
//        String hashKey = hashUtil.hash(String.valueOf(startNode.getId()));
//
//        // Create (tell) messages
//        NodeJoinMessage joinMessage = new NodeJoinMessage(startNode);
//        NodeLeaveMessage leaveMessage = new NodeLeaveMessage(startNode);
//
//        FingerTable.Get getFingerTable = new FingerTable.Get(hashId);
//
//        KeyValue.Put putValueMessage = new KeyValue.Put(hashKey, "This is some kind of test value");
//
//        // Send messages to the node
//        askForFingerTable(node, getFingerTable);
//
//        // Node is joining..
//        node.tell(joinMessage, ActorRef.noSender());
//        askForFingerTable(node, getFingerTable);
//
//        // Add en retrieve value
//        node.tell(putValueMessage, ActorRef.noSender());
//        askForValue(node, new KeyValue.Get(hashKey));
//
//        // Node is leaving after sometime...
//        node.tell(leaveMessage, ActorRef.noSender());
//        askForFingerTable(node, getFingerTable);

//        system.stop(node); // Quit node

//        system.terminate(); // Terminate application
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
