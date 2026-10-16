package com.fraudsentinel.scoring.rule.impl;

import com.fraudsentinel.scoring.config.FraudProperties;
import com.fraudsentinel.scoring.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class NightTimeRuleTest {

    private final NightTimeRule rule = new NightTimeRule(new FraudProperties(
            new FraudProperties.Rules(
                    new FraudProperties.HighAmount(true, new BigDecimal("50000000"), 60),
                    new FraudProperties.NightTime(true, 1, 4, "Asia/Ho_Chi_Minh", 20)),
            new FraudProperties.DecisionThresholds(50, 80)));

    private Transaction txnAt(String utc) {
        return new Transaction("T1", "ACC1", new BigDecimal("1000"), "VND",
                "MOBILE", "ACC2", "DEV1", "1.1.1.1", Instant.parse(utc));
    }

    @Test
    void hits_when_2h30_vietnam_time() {
        // 19:30 UTC = 02:30 giờ VN
        assertTrue(rule.evaluate(txnAt("2026-10-15T19:30:00Z")).hit());
    }

    @Test
    void passes_when_9h_vietnam_time() {
        // 02:00 UTC = 09:00 giờ VN
        assertFalse(rule.evaluate(txnAt("2026-10-15T02:00:00Z")).hit());
    }
}