package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.domain.Decision;
import com.fraudsentinel.scoring.domain.ScoreResult;
import com.fraudsentinel.scoring.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AlertWorkflowTest {

    static Alert newAlert() {
        String txnId = UUID.randomUUID().toString();
        Transaction txn = new Transaction(txnId, "ACC1", new BigDecimal("75000000"), "VND",
                "MOBILE", "ACC2", "DEV1", "1.1.1.1", Instant.now());
        ScoreResult result = new ScoreResult(txnId, 80, Decision.BLOCK, List.of("AMT_HIGH", "TIME_NIGHT"));
        return Alert.from(new RuleAlertEvent(txn, result, Instant.now()));
    }

    @Test
    void happy_path_assign_then_resolve() {
        Alert a = newAlert();
        a.assign("alice", Instant.now());
        a.resolve("alice", AlertStatus.CONFIRMED_FRAUD, "customer confirmed", Instant.now());
        assertEquals(AlertStatus.CONFIRMED_FRAUD, a.getStatus());
    }

    @Test
    void cannot_resolve_without_assigning() {
        Alert a = newAlert();
        assertThrows(AlertStateException.class,
                () -> a.resolve("alice", AlertStatus.FALSE_POSITIVE, null, Instant.now()));
    }

    @Test
    void only_assignee_can_resolve() {
        Alert a = newAlert();
        a.assign("alice", Instant.now());
        assertThrows(NotAssigneeException.class,
                () -> a.resolve("bob", AlertStatus.FALSE_POSITIVE, null, Instant.now()));
    }

    @Test
    void cannot_assign_twice() {
        Alert a = newAlert();
        a.assign("alice", Instant.now());
        assertThrows(AlertStateException.class, () -> a.assign("bob", Instant.now()));
    }

    @Test
    void release_returns_alert_to_queue() {
        Alert a = newAlert();
        a.assign("alice", Instant.now());
        a.release("alice");
        assertEquals(AlertStatus.OPEN, a.getStatus());
        assertNull(a.getAssignee());
    }
}