package com.fraudsentinel.scoring.rule.impl;

import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.feature.VelocityStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class VelocityRuleTest {

    private final VelocityRule.Params params = new VelocityRule.Params(60, 5);

    private VelocityRule ruleWithCount(long fakeCount) {
        VelocityStore fakeStore = (key, id, time, window) -> fakeCount;
        return new VelocityRule(fakeStore);
    }

    private final Transaction txn = new Transaction("T1", "ACC1", new BigDecimal("1000"), "VND",
            "MOBILE", "ACC2", "DEV1", "1.1.1.1", Instant.parse("2026-10-16T03:00:00Z"));

    @Test
    void passes_at_limit() {
        assertFalse(ruleWithCount(5).evaluate(txn, params, 50).hit());
    }

    @Test
    void hits_above_limit() {
        assertTrue(ruleWithCount(6).evaluate(txn, params, 50).hit());
    }
}