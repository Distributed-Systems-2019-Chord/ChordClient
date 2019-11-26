package org.distributed.systems.chord.repository;

import akka.actor.ActorSelection;
import org.distributed.systems.ChordStart;
import org.distributed.systems.chord.model.ChordNode;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static akka.pattern.Patterns.ask;

public class NodeRepository {

    private static class GetSuccessor {

    }

    private static class GetSuccessorReply {

        private final ChordNode successor;

        public GetSuccessorReply(ChordNode successor) {
            this.successor = successor;
        }

        public ChordNode getChordNode() {
            return this.successor;
        }
    }

    public ChordNode askForSuccessor(ActorSelection selectedActor) {
        // Prepare
        CompletableFuture<Object> successorRequest = ask(selectedActor, GetSuccessor.class, Duration.ofMillis(ChordStart.STANDARD_TIME_OUT)).toCompletableFuture();

        // Send
        CompletableFuture<GetSuccessorReply> transformed =
                CompletableFuture.allOf(successorRequest)
                        .thenApply(v -> (GetSuccessorReply) successorRequest.join());

        // Handle response
        final ChordNode[] response = {null};
        transformed.whenComplete((successorRequestReply, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            } else {
                response[0] = successorRequestReply.getChordNode();
            }
        });

        return response[0];
    }
}
