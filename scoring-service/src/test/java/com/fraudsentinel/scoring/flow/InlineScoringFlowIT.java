package com.fraudsentinel.scoring.flow;

import com.fraudsentinel.scoring.TestcontainersConfig;
import com.fraudsentinel.scoring.alert.*;
import com.fraudsentinel.scoring.domain.Decision;
import com.fraudsentinel.scoring.domain.ScoreResult;
import com.fraudsentinel.scoring.rule.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
class InlineScoringFlowIT {

    @Value("${local.server.port}") int port;
    @Autowired JsonMapper json;
    @Autowired AlertRepository alerts;
    @Autowired AlertAuditRepository audits;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void blocked_transaction_returns_decision_and_persists_an_inline_alert() throws Exception {
        String txnId = UUID.randomUUID().toString();
        // 75 triệu lúc 02:30 giờ VN → AMT_HIGH (60) + TIME_NIGHT (20) = 80 → BLOCK
        String body = """
                {"txnId":"%s","accountId":"ACC-%s","amount":75000000,"currency":"VND",
                 "channel":"MOBILE","timestamp":"2026-10-15T19:30:00Z"}
                """.formatted(txnId, txnId);

        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/score"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        // 1. Quyết định trả về đồng bộ cho core
        assertThat(response.statusCode()).isEqualTo(200);
        ScoreResult result = json.readValue(response.body(), ScoreResult.class);
        assertThat(result.decision()).isEqualTo(Decision.BLOCK);
        assertThat(result.reasons()).containsExactlyInAnyOrder("AMT_HIGH", "TIME_NIGHT");

        // 2. Alert đi qua rule-alerts → AlertConsumer → Postgres (bất đồng bộ, nên phải chờ)
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            Optional<Alert> found = alerts.findByTxnIdAndStage(txnId, Stage.INLINE);
            assertThat(found).isPresent();          // AssertionError → Awaitility chờ rồi thử lại
            Alert alert = found.get();
            assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
            assertThat(alert.getFundsMoved()).isNull();
            assertThat(audits.findByAlertIdOrderByCreatedAtAsc(alert.getId()))
                    .extracting(AlertAuditEntry::getAction)
                    .containsExactly("CREATED");
        });
    }
}