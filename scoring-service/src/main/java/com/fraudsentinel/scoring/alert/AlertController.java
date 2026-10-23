package com.fraudsentinel.scoring.alert;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private static final String ANALYST_HEADER = "X-Analyst-Id";

    private final AlertRepository repository;
    private final AlertCaseService caseService;

    public AlertController(AlertRepository repository, AlertCaseService caseService) {
        this.repository = repository;
        this.caseService = caseService;
    }

    @GetMapping
    public List<AlertView> list(@RequestParam(defaultValue = "OPEN") AlertStatus status) {
        return repository.findTop50ByStatusOrderByCreatedAtDesc(status)
                .stream().map(AlertView::of).toList();
    }

    @PostMapping("/{id}/assign")
    public AlertView assign(@PathVariable Long id, @RequestHeader(ANALYST_HEADER) String analyst) {
        return caseService.assign(id, analyst);
    }

    @PostMapping("/{id}/release")
    public AlertView release(@PathVariable Long id, @RequestHeader(ANALYST_HEADER) String analyst) {
        return caseService.release(id, analyst);
    }

    @PostMapping("/{id}/resolve")
    public AlertView resolve(@PathVariable Long id, @RequestHeader(ANALYST_HEADER) String analyst,
                             @Valid @RequestBody ResolveRequest request) {
        return caseService.resolve(id, analyst, request.outcome(), request.note());
    }

    @GetMapping("/{id}/history")
    public List<AlertAuditEntry> history(@PathVariable Long id) {
        return caseService.history(id);
    }

    @GetMapping("/stats/rules")
    public List<RuleStatsView> ruleStats() {
        Map<String, long[]> counts = new TreeMap<>();   // rule → [confirmed, falsePositive]
        for (AlertRepository.RuleOutcomeCount row : repository.countOutcomesByRule()) {
            long[] c = counts.computeIfAbsent(row.getRule(), k -> new long[2]);
            if ("CONFIRMED_FRAUD".equals(row.getOutcome())) c[0] += row.getTotal();
            else c[1] += row.getTotal();
        }
        return counts.entrySet().stream().map(e -> {
            long confirmed = e.getValue()[0], fp = e.getValue()[1];
            Double precision = (confirmed + fp) == 0 ? null : (double) confirmed / (confirmed + fp);
            return new RuleStatsView(e.getKey(), confirmed, fp, precision);
        }).toList();
    }
}