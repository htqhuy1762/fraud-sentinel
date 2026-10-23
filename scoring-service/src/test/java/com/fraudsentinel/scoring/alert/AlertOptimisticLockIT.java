package com.fraudsentinel.scoring.alert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AlertOptimisticLockIT {

    @Autowired AlertRepository repository;
    @Autowired PlatformTransactionManager txManager;

    @Test
    void second_analyst_with_stale_copy_is_rejected() {
        Long id = repository.save(AlertWorkflowTest.newAlert()).getId();

        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        assertThrows(ObjectOptimisticLockingFailureException.class, () ->
                tx.executeWithoutResult(aliceTx -> {
                    // Alice mở case: đọc version 0, thấy OPEN
                    Alert alicesCopy = repository.findById(id).orElseThrow();

                    // Trong lúc đó Bob nhận case trong transaction riêng và commit trước → version 1
                    tx.executeWithoutResult(bobTx ->
                            repository.findById(id).orElseThrow().assign("bob", Instant.now()));

                    // Alice vẫn thấy OPEN trên bản cũ của mình, nên assign() không báo lỗi...
                    alicesCopy.assign("alice", Instant.now());
                    // ...nhưng lúc commit: UPDATE ... WHERE version = 0 → 0 dòng → exception
                }));

        assertEquals("bob", repository.findById(id).orElseThrow().getAssignee());
    }
}