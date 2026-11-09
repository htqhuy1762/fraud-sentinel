package com.fraudsentinel.scoring.deadletter;

import com.fraudsentinel.scoring.TestcontainersConfig;
import com.fraudsentinel.scoring.domain.Decision;
import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.domain.TransactionCompletedEvent;
import com.fraudsentinel.scoring.domain.TxnStatus;
import com.fraudsentinel.scoring.history.TransactionHistoryRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestcontainersConfig.class)
class DeadLetterFlowIT {

    @Autowired KafkaTemplate<String, String> kafka;
    @Autowired JsonMapper json;
    @Autowired DeadLetterRepository deadLetters;
    @Autowired DeadLetterService service;
    @Autowired TransactionHistoryRepository history;
    @Value("${app.topics.transactions}") String topic;

    private static String newId() {
        return UUID.randomUUID().toString();
    }

    private DeadLetter awaitRecorded(String key) {
        await().atMost(Duration.ofSeconds(30)).until(() -> !deadLetters.findByMessageKey(key).isEmpty());
        List<DeadLetter> found = deadLetters.findByMessageKey(key);
        assertThat(found).hasSize(1);
        return found.get(0);
    }

    /** Đặt thẳng một message hợp lệ vào DLT, giống như lúc trước nó từng lỗi tạm thời (ví dụ DB sập). */
    private String parkValidTransactionInDlt(String txnId) throws Exception {
        String account = "ACC-" + txnId;
        Transaction txn = new Transaction(txnId, account, new BigDecimal("100000"), "VND",
                "MOBILE", "ACC-R", "DEV-1", "10.0.0.1", Instant.parse("2026-10-15T03:00:00Z"));
        String payload = json.writeValueAsString(new TransactionCompletedEvent(txn, Decision.APPROVE, TxnStatus.EXECUTED));

        ProducerRecord<String, String> record = new ProducerRecord<>(topic + "-dlt", account, payload);
        record.headers().add(KafkaHeaders.DLT_ORIGINAL_TOPIC, topic.getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaders.DLT_EXCEPTION_FQCN,
                "org.springframework.dao.DataAccessResourceFailureException".getBytes(StandardCharsets.UTF_8));
        kafka.send(record).get(5, TimeUnit.SECONDS);
        return account;
    }

    @Test
    void malformed_message_is_recorded_with_its_origin_and_reason() throws Exception {
        String key = newId();
        kafka.send(topic, key, "this is not json").get(5, TimeUnit.SECONDS);

        DeadLetter d = awaitRecorded(key);
        assertThat(d.getStatus()).isEqualTo(DeadLetterStatus.NEW);
        assertThat(d.getOriginalTopic()).isEqualTo(topic);
        assertThat(d.getOriginalOffset()).isNotNull();
        assertThat(d.getPayload()).isEqualTo("this is not json");
        assertThat(d.getExceptionClass()).isNotBlank();
    }

    @Test
    void replay_sends_the_message_back_to_its_original_topic_where_it_is_processed() throws Exception {
        String txnId = newId();
        DeadLetter d = awaitRecorded(parkValidTransactionInDlt(txnId));

        DeadLetterView replayed = service.replay(d.getId(), "ops-1");
        assertThat(replayed.status()).isEqualTo(DeadLetterStatus.REPLAYED);

        // Message về lại transactions.completed và được xử lý bình thường → có trong lịch sử giao dịch
        await().atMost(Duration.ofSeconds(30)).until(() -> history.existsById(txnId));

        // Không được replay lần hai
        assertThatThrownBy(() -> service.replay(d.getId(), "ops-2")).isInstanceOf(DeadLetterStateException.class);
    }

    @Test
    void discarded_message_can_no_longer_be_replayed() throws Exception {
        DeadLetter d = awaitRecorded(parkValidTransactionInDlt(newId()));

        DeadLetterView discarded = service.discard(d.getId(), "ops-1", "duplicate of a transaction already handled manually");
        assertThat(discarded.status()).isEqualTo(DeadLetterStatus.DISCARDED);
        assertThat(discarded.resolutionNote()).contains("handled manually");

        assertThatThrownBy(() -> service.replay(d.getId(), "ops-1")).isInstanceOf(DeadLetterStateException.class);
    }
}
