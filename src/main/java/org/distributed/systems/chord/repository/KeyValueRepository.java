package org.distributed.systems.chord.repository;

import akka.actor.ActorRef;
import org.distributed.systems.ChordStart;
import org.distributed.systems.chord.messaging.KeyValue;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static akka.pattern.Patterns.ask;

public class KeyValueRepository {

    public static void askForValue(ActorRef node, KeyValue.Get getValue) {
        // Prepare
        CompletableFuture<Object> valueRequest = ask(node, getValue, Duration.ofMillis(ChordStart.STANDARD_TIME_OUT)).toCompletableFuture();

        // Send
        CompletableFuture<KeyValue.Reply> transformed =
                CompletableFuture.allOf(valueRequest)
                        .thenApply(v -> (KeyValue.Reply) valueRequest.join());

        // Handle response
        transformed.whenComplete((getValueRequest, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            } else {
                System.out.println("Response for get value: " + getValueRequest.value.toString());
            }
        });
    }
}
