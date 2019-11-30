package org.distributed.systems;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import org.distributed.systems.chord.actors.Node;

import java.util.Map;

public class ChordStart {

    public static final int STANDARD_TIME_OUT = 1000;
    // FIXME 160 according to sha-1 but this is the max_length of a java long..
    public static final int m = 3; // Number of bits in key id's
    public static final long AMOUNT_OF_KEYS = Math.round(Math.pow(2, m));

    public static void main(String[] args) {
        // Create actor system
        ActorSystem system = ActorSystem.create("ChordNetwork"); // Setup actor system

        Config config = system.settings().config();
        
        String nodeType = config.getString("myapp.nodeType");
        if (System.getenv("CHORD_NODE_TYPE") != null) {
            nodeType = System.getenv("CHORD_NODE_TYPE");
        }

        System.out.println("\nRead All Variables:-\n");

        Map <String, String> map = System.getenv();
        for (Map.Entry <String, String> entry: map.entrySet()) {
            System.out.println("Variable Name:- " + entry.getKey() + " Value:- " + entry.getValue());
        }

        if (nodeType.equals("central")) {
            system.actorOf(Props.create(Node.class), "ChordActor0");
        } else {
            system.actorOf(Props.create(Node.class));
        }
    }
}
