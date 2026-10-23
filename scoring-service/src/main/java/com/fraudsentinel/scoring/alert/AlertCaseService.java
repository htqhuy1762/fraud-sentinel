package com.fraudsentinel.scoring.alert;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AlertCaseService {

    static final String SYSTEM = "system";

    private final AlertRepository alerts;
    private final AlertAuditRepository audits;

    public AlertCaseService(AlertRepository alerts, AlertAuditRepository audits) {
        this.alerts = alerts;
        this.audits = audits;
    }

    @Transactional
    public Alert open(RuleAlertEvent event) {
        Alert alert = alerts.save(Alert.from(event));
        audits.save(AlertAuditEntry.of(alert, "CREATED", null, SYSTEM, null, alert.getCreatedAt()));
        return alert;
    }

    @Transactional
    public AlertView assign(Long id, String analyst) {
        Alert alert = get(id);
        AlertStatus from = alert.getStatus();
        Instant now = Instant.now();
        alert.assign(analyst, now);
        audits.save(AlertAuditEntry.of(alert, "ASSIGNED", from, analyst, null, now));
        return AlertView.of(alert);
    }

    @Transactional
    public AlertView release(Long id, String analyst) {
        Alert alert = get(id);
        AlertStatus from = alert.getStatus();
        alert.release(analyst);
        audits.save(AlertAuditEntry.of(alert, "RELEASED", from, analyst, null, Instant.now()));
        return AlertView.of(alert);
    }

    @Transactional
    public AlertView resolve(Long id, String analyst, AlertStatus outcome, String note) {
        Alert alert = get(id);
        AlertStatus from = alert.getStatus();
        Instant now = Instant.now();
        alert.resolve(analyst, outcome, note, now);
        audits.save(AlertAuditEntry.of(alert, "RESOLVED", from, analyst, note, now));
        return AlertView.of(alert);
    }

    @Transactional(readOnly = true)
    public List<AlertAuditEntry> history(Long id) {
        get(id);
        return audits.findByAlertIdOrderByCreatedAtAsc(id);
    }

    private Alert get(Long id) {
        return alerts.findById(id).orElseThrow(() -> new AlertNotFoundException(id));
    }
}