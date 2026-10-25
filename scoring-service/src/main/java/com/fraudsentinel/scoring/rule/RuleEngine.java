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

    private final List<Rule<?>> rules;
    private final RuleConfigProvider configs;

    public RuleEngine(List<Rule<?>> rules, RuleConfigProvider configs) {
        this.rules = rules;
        this.configs = configs;
        log.info("Loaded {} rules: {}", rules.size(), rules.stream().map(Rule::code).toList());
    }

    public List<RuleResult> evaluate(Transaction txn) {
        List<RuleResult> hits = new ArrayList<>();
        for (Rule<?> rule : rules) {
            RuleConfig cfg = configs.get(rule.code());
            if (cfg == null || !cfg.enabled()) continue;
            try {
                RuleResult result = run(rule, txn, cfg);
                if (result.hit()) hits.add(result);
            } catch (Exception e) {
                log.error("Rule {} failed for txn {}", rule.code(), txn.txnId(), e);
            }
        }
        return hits;
    }

    // Helper generic để "bắt" kiểu P của từng rule, ép params về đúng kiểu
    private <P> RuleResult run(Rule<P> rule, Transaction txn, RuleConfig cfg) {
        return rule.evaluate(txn, rule.paramsType().cast(cfg.params()), cfg.score());
    }
}