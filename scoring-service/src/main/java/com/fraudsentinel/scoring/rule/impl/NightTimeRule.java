package com.fraudsentinel.scoring.rule.impl;

import com.fraudsentinel.scoring.config.FraudProperties;
import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.rule.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class NightTimeRule implements Rule {

    private final FraudProperties.NightTime cfg;
    private final ZoneId zone;

    public NightTimeRule(FraudProperties props) {
        this.cfg = props.rules().nightTime();
        this.zone = ZoneId.of(cfg.zone());
    }

    @Override public String code() { return "TIME_NIGHT"; }
    @Override public SubjectType subjectType() { return SubjectType.CUSTOMER; }
    @Override public boolean enabled() { return cfg.enabled(); }

    @Override
    public RuleResult evaluate(Transaction txn) {
        int localHour = txn.timestamp().atZone(zone).getHour();   // UTC → giờ VN
        if (localHour >= cfg.startHour() && localHour < cfg.endHour()) {
            return RuleResult.hit(code(), cfg.score(), "Transaction at %dh local time".formatted(localHour));
        }
        return RuleResult.pass(code());
    }
}