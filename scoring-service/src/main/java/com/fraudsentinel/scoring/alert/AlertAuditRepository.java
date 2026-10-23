package com.fraudsentinel.scoring.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertAuditRepository extends JpaRepository<AlertAuditEntry, Long> {

    List<AlertAuditEntry> findByAlertIdOrderByCreatedAtAsc(Long alertId);
}