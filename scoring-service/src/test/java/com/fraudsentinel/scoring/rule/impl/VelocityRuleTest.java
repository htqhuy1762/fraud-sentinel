package com.fraudsentinel.scoring.rule.impl;

import com.fraudsentinel.scoring.config.FraudProperties;
import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.feature.VelocityStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class VelocityRuleTest {

    private VelocityRule ruleWithCount(long fakeCount) {
        VelocityStore fakeStore = (key, id, time, window) -> fakeCount;   // giả lập Redis
        FraudProperties props = new FraudProperties(
                new FraudProperties.Rules(
                        new FraudProperties.HighAmount(true, new BigDecimal("50000000"), 60),
                        new FraudProperties.NightTime(true, 1, 4, "Asia/Ho_Chi_Minh", 20),
                        new FraudProperties.Velocity(true, 60, 5, 50)),
                new FraudProperties.DecisionThresholds(50, 80));
        return new VelocityRule(props, fakeStore);
    }

    private final Transaction txn = new Transaction("T1", "ACC1", new BigDecimal("1000"), "VND",
            "MOBILE", "ACC2", "DEV1", "1.1.1.1", Instant.parse("2026-10-16T03:00:00Z"));

    @Test
    void passes_at_limit() {
        assertFalse(ruleWithCount(5).evaluate(txn).hit());
    }

    @Test
    void hits_above_limit() {
        assertTrue(ruleWithCount(6).evaluate(txn).hit());
    }
}