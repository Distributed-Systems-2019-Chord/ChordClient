package org.distributed.systems.chord.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;

import java.util.concurrent.CompletableFuture;


public class Node extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log.info("Starting up...");

        Config config = getContext().getSystem().settings().config();
        final String nodeType = config.getString("myapp.nodeType");
        log.info("DEBUG -- nodetype: " + nodeType);
        if(nodeType == "regular"){
            final String centralEntityAddress = config.getString("myapp.centralEntityAddress");
            ActorSelection selection = getContext().actorSelection("akka.tcp://ChordNetwork@" + centralEntityAddress + "/user/");
//          get fingertable from central entity
            selection.tell("newNode", getSelf());
//            CompletableFuture<Object> future = getContext().ask(selection,
//                    new fingerTableActor.getFingerTable(line), 1000).toCompletableFuture();
        }
    }

    @Override
    public Receive createReceive() {
        log.info("Received a message");

        return receiveBuilder()
                .matchEquals("printit", p -> {
                    log.info("The address of this actor is: " + getSelf()); // Log my reference
                    getSender().tell("Got Message", getSelf()); // Acknowledge
                }).build();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        log.info("Shutting down...");
    }

}
