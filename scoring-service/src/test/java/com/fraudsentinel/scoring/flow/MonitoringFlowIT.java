package com.fraudsentinel.scoring.flow;

import com.fraudsentinel.scoring.TestcontainersConfig;
import com.fraudsentinel.scoring.alert.Alert;
import com.fraudsentinel.scoring.alert.AlertRepository;
import com.fraudsentinel.scoring.domain.*;
import com.fraudsentinel.scoring.domain.Stage;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestcontainersConfig.class)
class MonitoringFlowIT {

    @Autowired KafkaTemplate<String, String> kafka;
    @Autowired ConsumerFactory<String, String> consumerFactory;
    @Autowired JsonMapper json;
    @Autowired AlertRepository alerts;
    @Value("${app.topics.transactions}") String topic;

    private static String newId() {
        return UUID.randomUUID().toString();
    }

    /** 10:00 sáng giờ VN → TIME_NIGHT không bị hit. */
    private static Transaction txn(String txnId, String accountId, long amount) {
        return new Transaction(txnId, accountId, BigDecimal.valueOf(amount), "VND",
                "MOBILE", "ACC-RECEIVER", "DEV-1", "10.0.0.1", Instant.parse("2026-10-15T03:00:00Z"));
    }

    private void publish(TransactionCompletedEvent event) throws Exception {
        kafka.send(topic, event.transaction().accountId(), json.writeValueAsString(event))
                .get(5, TimeUnit.SECONDS);
    }

    @Test
    void unscored_executed_transaction_is_caught_up_and_flagged_as_funds_moved() throws Exception {
        String txnId = newId();
        // Core không nhận được quyết định (inlineDecision = null) nhưng vẫn cho giao dịch đi
        publish(new TransactionCompletedEvent(txn(txnId, "ACC-" + txnId, 75_000_000), null, TxnStatus.EXECUTED));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            Optional<Alert> found = alerts.findByTxnIdAndStage(txnId, Stage.MONITORING);
            assertThat(found).isPresent();
            Alert alert = found.get();
            assertThat(alert.getFundsMoved()).isTrue();
            assertThat(alert.getReasons()).isEqualTo("AMT_HIGH");
            assertThat(alert.getDecision()).isEqualTo(Decision.REVIEW);
        });
    }

    @Test
    void transaction_already_scored_inline_gets_no_monitoring_alert() throws Exception {
        String account = "ACC-" + newId();
        String scoredTxn = newId();
        String markerTxn = newId();

        // 1. Giao dịch lớn ĐÃ được chấm inline → luồng monitoring không được chạy bù
        publish(new TransactionCompletedEvent(txn(scoredTxn, account, 75_000_000), Decision.REVIEW, TxnStatus.EXECUTED));
        // 2. "Cột mốc": cùng tài khoản (cùng partition, xử lý SAU), chưa được chấm → chắc chắn sinh alert
        publish(new TransactionCompletedEvent(txn(markerTxn, account, 75_000_000), null, TxnStatus.EXECUTED));

        // Khi alert của cột mốc đã xuất hiện, chắc chắn giao dịch 1 đã được xử lý xong
        await().atMost(Duration.ofSeconds(30))
                .until(() -> alerts.findByTxnIdAndStage(markerTxn, Stage.MONITORING).isPresent());

        assertThat(alerts.findByTxnIdAndStage(scoredTxn, Stage.MONITORING)).isEmpty();
    }

    @Test
    void malformed_message_goes_to_dead_letter_topic() throws Exception {
        String key = newId();
        kafka.send(topic, key, "this is not json").get(5, TimeUnit.SECONDS);

        try (Consumer<String, String> dlt = consumerFactory.createConsumer("dlt-check-" + key, null)) {
            dlt.subscribe(List.of(topic + "-dlt"));
            await().atMost(Duration.ofSeconds(30)).until(() -> {
                for (ConsumerRecord<String, String> r : dlt.poll(Duration.ofMillis(500))) {
                    if (key.equals(r.key())) return true;
                }
                return false;
            });
        }
    }
}