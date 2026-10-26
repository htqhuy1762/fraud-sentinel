package com.fraudsentinel.simulator;

import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Component
public class ScoringClient {

    private record ScoreResponse(String decision) {}

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
    private final SimulatorProperties props;
    private final JsonMapper json;

    public ScoringClient(SimulatorProperties props, JsonMapper json) {
        this.props = props;
        this.json = json;
    }

    /** @return quyết định inline, hoặc rỗng nếu hệ thống fraud không trả lời được */
    public Optional<String> score(SimTransaction txn) {
        HttpRequest request = HttpRequest.newBuilder(props.scoringUrl())
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(txn)))
                .build();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Optional.empty();
            }
            return Optional.ofNullable(json.readValue(response.body(), ScoreResponse.class).decision());
        } catch (IOException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
}