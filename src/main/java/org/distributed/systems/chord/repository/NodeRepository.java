package org.distributed.systems.chord.repository;

import akka.actor.ActorContext;
import akka.actor.ActorSelection;
import org.distributed.systems.ChordStart;
import org.distributed.systems.chord.messaging.FingerTable;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.util.Util;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static akka.pattern.Patterns.ask;

public class NodeRepository {

    public CompletableFuture<FingerTable.GetPredecessorReply> askForPredecessor(ActorContext context, ChordNode finger) {
        ActorSelection selectedActor = Util.getActorRef(context, finger);
        // Prepare
        CompletableFuture<Object> predecessorRequest = ask(selectedActor, FingerTable.GetPredecessor.class, Duration.ofMillis(ChordStart.STANDARD_TIME_OUT)).toCompletableFuture();

        // Handle response
        return CompletableFuture.allOf(predecessorRequest)
                .thenApply(v -> (FingerTable.GetPredecessorReply) predecessorRequest.join());
    }

    public CompletableFuture<FingerTable.GetSuccessorReply> askForSuccessor(ActorContext context, ChordNode node) {
        ActorSelection selectedActor = Util.getActorRef(context, node);
        return askForSuccessor(selectedActor, node.getId());
    }

    public CompletableFuture<FingerTable.GetSuccessorReply> askForSuccessor(ActorSelection selectedActor, long id) {

        // Prepare
        CompletableFuture<Object> successorRequest = ask(selectedActor, new FingerTable.GetSuccessor(id), Duration.ofMillis(ChordStart.STANDARD_TIME_OUT)).toCompletableFuture();

        // Handle response
        return CompletableFuture.allOf(successorRequest)
                .thenApply(v -> (FingerTable.GetSuccessorReply) successorRequest.join());
    }

    public CompletableFuture<FingerTable.FindSuccessorReply> askForFindingSuccessor(ActorSelection selectedActor, long id) {

        // Prepare
        CompletableFuture<Object> successorSearchRequest = ask(selectedActor, new FingerTable.FindSuccessor(id), Duration.ofMillis(ChordStart.STANDARD_TIME_OUT)).toCompletableFuture();

        // Handle response
        return CompletableFuture.allOf(successorSearchRequest)
                .thenApply(v -> (FingerTable.FindSuccessorReply) successorSearchRequest.join());
    }

    public CompletableFuture<FingerTable.GetClosestPrecedingFingerReply> askForClosestPrecedingFinger(ActorSelection selectedActor, long id) {

        // Prepare
        CompletableFuture<Object> closestPrecedingFingerRequest = ask(selectedActor, new FingerTable.GetClosestPrecedingFinger(id), Duration.ofMillis(ChordStart.STANDARD_TIME_OUT)).toCompletableFuture();

        // Handle response
        return CompletableFuture.allOf(closestPrecedingFingerRequest)
                .thenApply(v -> (FingerTable.GetClosestPrecedingFingerReply) closestPrecedingFingerRequest.join());
    }
}
