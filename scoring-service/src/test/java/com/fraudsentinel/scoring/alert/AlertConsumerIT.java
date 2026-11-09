package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.domain.RuleAlertEvent;
import com.fraudsentinel.scoring.TestcontainersConfig;
import com.fraudsentinel.scoring.domain.Decision;
import com.fraudsentinel.scoring.domain.ScoreResult;
import com.fraudsentinel.scoring.domain.Transaction;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestcontainersConfig.class)
class AlertConsumerIT {

    @Autowired KafkaTemplate<String, String> kafka;
    @Autowired ConsumerFactory<String, String> consumerFactory;
    @Autowired JsonMapper json;
    @Autowired AlertRepository alerts;
    @Value("${app.topics.alerts}") String alertsTopic;

    /**
     * Trước bản sửa: mọi vi phạm ràng buộc đều bị coi là "trùng" và alert bị bỏ âm thầm.
     * Sau bản sửa: vi phạm KHÁC trùng (ở đây currency dài hơn VARCHAR(3)) phải được retry
     * rồi đẩy sang rule-alerts-dlt, để có người xử lý.
     */
    @Test
    void alert_that_violates_a_non_unique_constraint_goes_to_dlt_instead_of_being_dropped() throws Exception {
        String txnId = UUID.randomUUID().toString();
        String account = "ACC-" + txnId;
        Transaction txn = new Transaction(txnId, account, new BigDecimal("75000000"), "TOOLONG",
                "MOBILE", "ACC-R", "DEV-1", "10.0.0.1", Instant.now());
        ScoreResult result = new ScoreResult(txnId, 80, Decision.BLOCK, List.of("AMT_HIGH", "TIME_NIGHT"));
        RuleAlertEvent event = new RuleAlertEvent(txn, result, Stage.INLINE, null, Instant.now());

        kafka.send(alertsTopic, account, json.writeValueAsString(event)).get(5, TimeUnit.SECONDS);

        try (Consumer<String, String> dlt = consumerFactory.createConsumer("alert-dlt-check-" + txnId, null)) {
            dlt.subscribe(List.of(alertsTopic + "-dlt"));
            await().atMost(Duration.ofSeconds(30)).until(() -> {
                for (ConsumerRecord<String, String> r : dlt.poll(Duration.ofMillis(500))) {
                    if (account.equals(r.key())) return true;
                }
                return false;
            });
        }
        assertThat(alerts.findByTxnIdAndStage(txnId, Stage.INLINE)).isEmpty();
    }
}
