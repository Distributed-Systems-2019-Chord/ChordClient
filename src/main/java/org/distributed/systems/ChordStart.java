package org.distributed.systems;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import org.distributed.systems.chord.actors.Node;

public class ChordStart {

    public static final int STANDARD_TIME_OUT = 1000;
    // FIXME 160 according to sha-1 but this is the max_length of a java long..
    public static final int M = 64;

    public static void main(String[] args) {
        // Create actor system
        ActorSystem system = ActorSystem.create("ChordNetwork"); // Setup actor system

        Config config = system.settings().config();
        final String nodeType = config.getString("myapp.nodeType");

        if (nodeType.equals("central")) {
            system.actorOf(Props.create(Node.class, "central"), "ChordActor");
        } else {
            system.actorOf(Props.create(Node.class, "regular"), "ChordActor");
        }
    }
}
