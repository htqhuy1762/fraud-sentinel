package com.fraudsentinel.scoring.messaging;

import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class KafkaSendSupport {

    private KafkaSendSupport() {}

    public static void await(CompletableFuture<? extends SendResult<?, ?>> future, String what) {
        try {
            future.get(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaPublishException("Interrupted while publishing " + what, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new KafkaPublishException("Failed to publish " + what, e);
        }
    }
}