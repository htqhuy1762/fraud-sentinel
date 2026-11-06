package com.fraudsentinel.scoring.flow;

import com.fraudsentinel.scoring.TestcontainersConfig;
import com.fraudsentinel.scoring.domain.*;
import com.fraudsentinel.scoring.history.HistoryPage;
import com.fraudsentinel.scoring.history.TransactionHistoryRepository;
import com.fraudsentinel.scoring.history.TransactionHistoryService;
import com.fraudsentinel.scoring.history.TransactionView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(TestcontainersConfig.class)
class TransactionHistoryFlowIT {

    @Autowired KafkaTemplate<String, String> kafka;
    @Autowired JsonMapper json;
    @Autowired TransactionHistoryRepository repository;
    @Autowired TransactionHistoryService history;
    @Value("${app.topics.transactions}") String topic;

    private static String newId() {
        return UUID.randomUUID().toString();
    }

    private void publish(String txnId, String account, Instant time) throws Exception {
        Transaction txn = new Transaction(txnId, account, new BigDecimal("100000"), "VND",
                "MOBILE", "ACC-RECEIVER", "DEV-1", "10.0.0.1", time);
        TransactionCompletedEvent event = new TransactionCompletedEvent(txn, Decision.APPROVE, TxnStatus.EXECUTED);
        kafka.send(topic, account, json.writeValueAsString(event)).get(5, TimeUnit.SECONDS);
    }

    @Test
    void duplicate_event_is_recorded_once_and_does_not_block_the_partition() throws Exception {
        String account = "ACC-" + newId();
        String txnId = newId();
        String marker = newId();
        Instant t = Instant.parse("2026-10-15T03:00:00Z");

        publish(txnId, account, t);
        publish(txnId, account, t);                     // trùng: phải bị bỏ qua êm, không retry, không DLT
        publish(marker, account, t.plusSeconds(1));     // cột mốc cùng partition, được xử lý sau

        await().atMost(Duration.ofSeconds(30)).until(() -> repository.existsById(marker));
        assertThat(repository.existsById(txnId)).isTrue();
        assertThat(history.page(account, 50, null, null).items()).hasSize(2);
    }

    @Test
    void history_is_paged_newest_first_with_a_cursor() throws Exception {
        String account = "ACC-" + newId();
        Instant base = Instant.parse("2026-10-15T03:00:00Z");
        String oldest = newId(), middle = newId(), newest = newId();
        publish(oldest, account, base);
        publish(middle, account, base.plusSeconds(60));
        publish(newest, account, base.plusSeconds(120));

        await().atMost(Duration.ofSeconds(30)).until(() -> repository.existsById(newest));

        HistoryPage first = history.page(account, 2, null, null);
        assertThat(first.items()).extracting(TransactionView::txnId).containsExactly(newest, middle);
        assertThat(first.next()).isNotNull();

        HistoryPage second = history.page(account, 2, first.next().beforeTime(), first.next().beforeTxnId());
        assertThat(second.items()).extracting(TransactionView::txnId).containsExactly(oldest);
        assertThat(second.next()).isNull();
    }
}