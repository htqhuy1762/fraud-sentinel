package com.fraudsentinel.scoring.deadletter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeadLetterRepository extends JpaRepository<DeadLetter, Long> {

    boolean existsByDltTopicAndDltPartitionAndDltOffset(String dltTopic, int dltPartition, long dltOffset);

    List<DeadLetter> findTop100ByStatusOrderByReceivedAtDesc(DeadLetterStatus status);

    List<DeadLetter> findByMessageKey(String messageKey);
}
