package com.fraudsentinel.scoring.scoring;

import com.fraudsentinel.scoring.alert.RuleAlertEvent;
import com.fraudsentinel.scoring.config.FraudProperties;
import com.fraudsentinel.scoring.domain.*;
import com.fraudsentinel.scoring.rule.RuleEngine;
import com.fraudsentinel.scoring.rule.RuleResult;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ScoringService {

    private final RuleEngine ruleEngine;
    private final FraudProperties.DecisionThresholds thresholds;
    private final ApplicationEventPublisher publisher;

    public ScoringService(RuleEngine ruleEngine, FraudProperties props, ApplicationEventPublisher publisher) {
        this.ruleEngine = ruleEngine;
        this.thresholds = props.decision();
        this.publisher = publisher;
    }

    public ScoreResult score(Transaction txn) {
        List<RuleResult> hits = ruleEngine.evaluate(txn);

        int score = Math.min(100, hits.stream().mapToInt(RuleResult::score).sum());
        Decision decision = decide(score);
        List<String> reasons = hits.stream().map(RuleResult::code).toList();

        ScoreResult result = new ScoreResult(txn.txnId(), score, decision, reasons);

        if (decision != Decision.APPROVE) {
            publisher.publishEvent(new RuleAlertEvent(txn, result, Instant.now()));
        }
        return result;
    }

    private Decision decide(int score) {
        if (score >= thresholds.blockThreshold()) return Decision.BLOCK;
        if (score >= thresholds.reviewThreshold()) return Decision.REVIEW;
        return Decision.APPROVE;
    }
}