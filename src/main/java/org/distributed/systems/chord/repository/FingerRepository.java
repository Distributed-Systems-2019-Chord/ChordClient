package org.distributed.systems.chord.repository;

import akka.actor.ActorSelection;
import org.distributed.systems.ChordStart;
import org.distributed.systems.chord.messaging.FingerTable;
import org.distributed.systems.chord.service.FingerTableService;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static akka.pattern.Patterns.ask;

public class FingerRepository {

    public static void askForFingerTable(ActorSelection node, FingerTable.Get getFingerTable, FingerTableService fingerTableService) {
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
                fingerTableService.handleFingerTableMessage(getFingerTableMessage);

                System.out.println("Response for get finger table: " +
                        "successor: " + getFingerTableMessage.successors + "\n" +
                        "predecessor: " + getFingerTableMessage.predecessor
                );
            }
        });
    }
}
