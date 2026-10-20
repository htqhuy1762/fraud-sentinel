package com.fraudsentinel.scoring.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    boolean existsByTxnId(String txnId);

    List<Alert> findTop50ByStatusOrderByCreatedAtDesc(AlertStatus status);
}