package com.fraudsentinel.scoring.rule.impl;

import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.rule.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class NightTimeRule implements Rule<NightTimeRule.Params> {

    public record Params(int startHour, int endHour, String zone) {
        public Params {
            if (startHour < 0 || startHour > 23 || endHour < 0 || endHour > 23 || startHour == endHour) {
                throw new IllegalArgumentException("hours must be 0-23 and startHour != endHour");
            }
            ZoneId.of(zone);   // ném exception nếu zone không hợp lệ
        }
    }

    @Override public String code() { return "TIME_NIGHT"; }
    @Override public SubjectType subjectType() { return SubjectType.CUSTOMER; }
    @Override public Class<Params> paramsType() { return Params.class; }

    @Override
    public RuleResult evaluate(Transaction txn, Params p, int score) {
        int hour = txn.timestamp().atZone(ZoneId.of(p.zone())).getHour();
        boolean inWindow = p.startHour() < p.endHour()
                ? hour >= p.startHour() && hour < p.endHour()      // ví dụ 1h → 4h
                : hour >= p.startHour() || hour < p.endHour();     // vắt qua nửa đêm, ví dụ 22h → 2h
        if (inWindow) {
            return RuleResult.hit(code(), score, "Transaction at %dh local time".formatted(hour));
        }
        return RuleResult.pass(code());
    }
}