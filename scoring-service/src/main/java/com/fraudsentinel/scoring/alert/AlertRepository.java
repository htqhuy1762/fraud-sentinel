package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.rule.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    boolean existsByTxnIdAndStage(String txnId, Stage stage);

    List<Alert> findTop50ByStatusOrderByCreatedAtDesc(AlertStatus status);

    interface RuleOutcomeCount {
        String getRule();
        String getOutcome();
        long getTotal();
    }

    @Query(value = """
            SELECT r.rule      AS rule,
                   a.status    AS outcome,
                   COUNT(*)    AS total
            FROM fraud_alert a
            CROSS JOIN LATERAL unnest(string_to_array(a.reasons, ',')) AS r(rule)
            WHERE a.status IN ('CONFIRMED_FRAUD', 'FALSE_POSITIVE')
            GROUP BY r.rule, a.status
            """, nativeQuery = true)
    List<RuleOutcomeCount> countOutcomesByRule();
}