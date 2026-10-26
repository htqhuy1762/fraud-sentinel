package com.fraudsentinel.scoring.scoring;

import com.fraudsentinel.scoring.alert.RuleAlertEvent;
import com.fraudsentinel.scoring.config.FraudProperties;
import com.fraudsentinel.scoring.domain.*;
import com.fraudsentinel.scoring.rule.RuleEngine;
import com.fraudsentinel.scoring.rule.RuleResult;
import com.fraudsentinel.scoring.rule.Stage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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

    /** Đường INLINE: core đang chờ quyết định trước khi thực hiện giao dịch. */
    public ScoreResult score(Transaction txn) {
        ScoreResult result = evaluate(txn, EnumSet.of(Stage.INLINE));
        if (result.decision() != Decision.APPROVE) {
            publisher.publishEvent(new RuleAlertEvent(txn, result, Stage.INLINE, null, Instant.now()));
        }
        return result;
    }

    /** Đường MONITORING: giao dịch đã hoàn tất, không còn gì để duyệt nữa. */
    public void monitor(TransactionCompletedEvent event) {
        boolean scoredInline = event.inlineDecision() != null;
        // Chưa được chấm inline → chạy bù cả rule INLINE, để không giao dịch nào thoát hoàn toàn
        Set<Stage> stages = scoredInline ? EnumSet.of(Stage.MONITORING) : EnumSet.allOf(Stage.class);

        ScoreResult result = evaluate(event.transaction(), stages);
        if (result.decision() != Decision.APPROVE) {
            boolean fundsMoved = event.status() == TxnStatus.EXECUTED;
            publisher.publishEvent(new RuleAlertEvent(event.transaction(), result, Stage.MONITORING,
                    fundsMoved, Instant.now()));
        }
    }

    private ScoreResult evaluate(Transaction txn, Set<Stage> stages) {
        List<RuleResult> hits = ruleEngine.evaluate(txn, stages);
        int score = Math.min(100, hits.stream().mapToInt(RuleResult::score).sum());
        List<String> reasons = hits.stream().map(RuleResult::code).toList();
        return new ScoreResult(txn.txnId(), score, decide(score), reasons);
    }

    private Decision decide(int score) {
        if (score >= thresholds.blockThreshold()) return Decision.BLOCK;
        if (score >= thresholds.reviewThreshold()) return Decision.REVIEW;
        return Decision.APPROVE;
    }
}