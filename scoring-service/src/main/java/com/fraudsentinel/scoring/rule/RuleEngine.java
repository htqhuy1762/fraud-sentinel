package com.fraudsentinel.scoring.rule;

import com.fraudsentinel.scoring.domain.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    private final List<Rule> rules;

    public RuleEngine(List<Rule> rules) {   // Spring tự inject mọi bean implement Rule
        this.rules = rules;
        log.info("Loaded {} rules: {}", rules.size(), rules.stream().map(Rule::code).toList());
    }

    public List<RuleResult> evaluate(Transaction txn) {
        List<RuleResult> hits = new ArrayList<>();
        for (Rule rule : rules) {
            if (!rule.enabled()) continue;
            try {
                RuleResult result = rule.evaluate(txn);
                if (result.hit()) hits.add(result);
            } catch (Exception e) {
                // 1 rule lỗi không được làm sập cả request
                log.error("Rule {} failed for txn {}", rule.code(), txn.txnId(), e);
            }
        }
        return hits;
    }
}