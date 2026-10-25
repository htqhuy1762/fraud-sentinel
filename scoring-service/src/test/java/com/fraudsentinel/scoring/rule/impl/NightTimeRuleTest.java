package com.fraudsentinel.scoring.rule.impl;

import com.fraudsentinel.scoring.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class NightTimeRuleTest {

    private final NightTimeRule rule = new NightTimeRule();
    private final NightTimeRule.Params oneToFour = new NightTimeRule.Params(1, 4, "Asia/Ho_Chi_Minh");
    private final NightTimeRule.Params tenPmToTwoAm = new NightTimeRule.Params(22, 2, "Asia/Ho_Chi_Minh");

    private Transaction txnAt(String utc) {
        return new Transaction("T1", "ACC1", new BigDecimal("1000"), "VND",
                "MOBILE", "ACC2", "DEV1", "1.1.1.1", Instant.parse(utc));
    }

    @Test
    void hits_when_2h30_vietnam_time() {
        assertTrue(rule.evaluate(txnAt("2026-10-15T19:30:00Z"), oneToFour, 20).hit());   // 02:30 VN
    }

    @Test
    void passes_when_9h_vietnam_time() {
        assertFalse(rule.evaluate(txnAt("2026-10-15T02:00:00Z"), oneToFour, 20).hit());  // 09:00 VN
    }

    @Test
    void window_crossing_midnight_hits_before_and_after_midnight() {
        assertTrue(rule.evaluate(txnAt("2026-10-15T16:30:00Z"), tenPmToTwoAm, 20).hit()); // 23:30 VN
        assertTrue(rule.evaluate(txnAt("2026-10-15T18:30:00Z"), tenPmToTwoAm, 20).hit()); // 01:30 VN
        assertFalse(rule.evaluate(txnAt("2026-10-15T20:00:00Z"), tenPmToTwoAm, 20).hit()); // 03:00 VN
    }

    @Test
    void invalid_params_are_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new NightTimeRule.Params(25, 4, "Asia/Ho_Chi_Minh"));
        assertThrows(Exception.class, () -> new NightTimeRule.Params(1, 4, "Mars/Olympus"));
    }
}