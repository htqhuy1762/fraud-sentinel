package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.messaging.KafkaSendSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AlertPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper json;
    private final String alertsTopic;

    public AlertPublisher(KafkaTemplate<String, String> kafka, JsonMapper json,
                          @Value("${app.topics.alerts}") String alertsTopic) {
        this.kafka = kafka;
        this.json = json;
        this.alertsTopic = alertsTopic;
    }

    @EventListener   // KHÔNG còn @Async: lỗi ở đây phải lan ngược về ScoringService
    public void onAlert(RuleAlertEvent event) {
        KafkaSendSupport.await(
                kafka.send(alertsTopic, event.transaction().accountId(), json.writeValueAsString(event)),
                "alert for txn " + event.transaction().txnId());
    }
}