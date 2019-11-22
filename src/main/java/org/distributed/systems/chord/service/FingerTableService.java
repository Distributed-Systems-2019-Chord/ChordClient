package org.distributed.systems.chord.service;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import org.distributed.systems.ChordStart;
import org.distributed.systems.chord.messaging.FingerTable;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static akka.pattern.Patterns.ask;

public class FingerTableService {
    ActorRef successor;
    ActorRef predecessor;

    public ActorRef getSuccessor() {
        return successor;
    }

    public ActorRef getPredecessor() {
        return predecessor;
    }

    public void setSuccessor(ActorRef successor) {
        this.successor = successor;
    }

    public void setPredecessor(ActorRef predecessor) {
        this.predecessor = predecessor;
    }

    public void askForFingerTable(ActorSelection node, FingerTable.Get getFingerTable) {
        // Prepare
        CompletableFuture<Object> fingerTableRequest = ask(node, getFingerTable, Duration.ofMillis(ChordStart.STANDARD_TIME_OUT)).toCompletableFuture();

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
                        "successor: " + getFingerTableMessage.successor + "\n" +
                        "predecessor: " + getFingerTableMessage.predecessor
                );
            }
        });
    }


}
