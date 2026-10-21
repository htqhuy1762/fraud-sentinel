package com.fraudsentinel.simulator;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SimulatorRunner {

    private static final Logger log = LoggerFactory.getLogger(SimulatorRunner.class);

    private final SimulatorProperties props;
    private final TransactionGenerator generator;
    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper json;

    private final Map<String, AtomicLong> sent = new ConcurrentHashMap<>();
    private long ticks = 0;

    public SimulatorRunner(SimulatorProperties props, TransactionGenerator generator,
                           KafkaTemplate<String, String> kafka, JsonMapper json) {
        this.props = props;
        this.generator = generator;
        this.kafka = kafka;
        this.json = json;
    }

    @Scheduled(fixedRate = 1000)
    public void tick() {
        for (int i = 0; i < props.tps(); i++) {
            double r = ThreadLocalRandom.current().nextDouble();
            if (r < props.fraudRate() / 2) {
                send(generator.highAmount(), "HIGH_AMOUNT");
            } else if (r < props.fraudRate()) {
                generator.burst().forEach(t -> send(t, "BURST"));
            } else {
                send(generator.normal(), "NORMAL");
            }
        }
        if (++ticks % 10 == 0) {
            log.info("Sent so far: {}", sent);
        }
    }

    private void send(SimTransaction txn, String scenario) {
        ProducerRecord<String, String> record =
                new ProducerRecord<>(props.topic(), txn.accountId(), json.writeValueAsString(txn));
        record.headers().add("x-sim-scenario", scenario.getBytes(StandardCharsets.UTF_8));
        kafka.send(record);
        sent.computeIfAbsent(scenario, k -> new AtomicLong()).incrementAndGet();
    }
}