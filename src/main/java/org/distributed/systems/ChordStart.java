package org.distributed.systems;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import org.distributed.systems.chord.actors.Node;
import org.distributed.systems.chord.util.IHashUtil;
import org.distributed.systems.chord.util.impl.HashUtil;

public class ChordStart {

    public static final int STANDARD_TIME_OUT = 5000;
    // FIXME 160 according to sha-1 but this is the max_length of a java long..
    public static final int m = 16; // Number of bits in key id's
    public static final long AMOUNT_OF_KEYS = Math.round(Math.pow(2, m));

    public static ActorRef node;
    private static IHashUtil hashUtil = new HashUtil();
    public static final long NODE_ID = 1L;//hashUtil.hash(System.getProperty("hostname"));

    public static void main(String[] args) {
        IHashUtil hashUtil = new HashUtil();

        // Create actor system
        ActorSystem system = ActorSystem.create("ChordNetwork"); // Setup actor system

        // Create start node
//        ChordNode startNode = new ChordNode(0L);
        //ChordNode startNode = new ChordNode(1L, "127.0.0.1", 2551);


        Config config = system.settings().config();
        final String nodeType = config.getString("myapp.nodeType");

        if (nodeType.equals("central")) {
            node = system.actorOf(Props.create(Node.class), "ChordActor0");
        } else {
            node = system.actorOf(Props.create(Node.class));
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
}
