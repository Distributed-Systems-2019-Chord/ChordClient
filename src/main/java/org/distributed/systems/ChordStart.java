package org.distributed.systems;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.distributed.systems.chord.node.Node;

public class ChordStart {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ChordNetwork"); // Setup actor system
        ActorRef node = system.actorOf(Props.create(Node.class)); // Create new actor: node

        node.tell("JOIN", ActorRef.noSender()); // Send message

        system.stop(node); // Quit node

        system.terminate(); // Terminate application
    }
}
