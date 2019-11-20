package org.distributed.systems.chord.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Node extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log.info("Starting up...");
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
