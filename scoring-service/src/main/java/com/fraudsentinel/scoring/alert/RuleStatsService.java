package com.fraudsentinel.scoring.alert;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Tổng hợp kết luận của analyst thành độ chính xác (precision) của từng rule. */
@Service
public class RuleStatsService {

    private final AlertRepository repository;

    public RuleStatsService(AlertRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RuleStatsView> ruleStats() {
        Map<String, long[]> counts = new TreeMap<>();   // rule → [confirmed, falsePositive]
        for (AlertRepository.RuleOutcomeCount row : repository.countOutcomesByRule()) {
            long[] c = counts.computeIfAbsent(row.getRule(), k -> new long[2]);
            if (AlertStatus.CONFIRMED_FRAUD.name().equals(row.getOutcome())) c[0] += row.getTotal();
            else c[1] += row.getTotal();
        }
        return counts.entrySet().stream().map(e -> {
            long confirmed = e.getValue()[0], falsePositive = e.getValue()[1];
            long total = confirmed + falsePositive;
            Double precision = total == 0 ? null : (double) confirmed / total;
            return new RuleStatsView(e.getKey(), confirmed, falsePositive, precision);
        }).toList();
    }
}
