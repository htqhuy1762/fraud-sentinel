package com.fraudsentinel.scoring.deadletter;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "dead_letter")
public class DeadLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dltTopic;
    private int dltPartition;
    private long dltOffset;

    private String originalTopic;
    private Integer originalPartition;
    private Long originalOffset;

    private String messageKey;
    private String payload;
    private String exceptionClass;
    private String exceptionMessage;

    @Enumerated(EnumType.STRING)
    private DeadLetterStatus status;

    private Instant receivedAt;
    private String resolvedBy;
    private Instant resolvedAt;
    private String resolutionNote;

    @Version
    private Long version;

    protected DeadLetter() {}

    static DeadLetter received(String dltTopic, int dltPartition, long dltOffset,
                               String originalTopic, Integer originalPartition, Long originalOffset,
                               String messageKey, String payload,
                               String exceptionClass, String exceptionMessage, Instant receivedAt) {
        DeadLetter d = new DeadLetter();
        d.dltTopic = dltTopic;
        d.dltPartition = dltPartition;
        d.dltOffset = dltOffset;
        d.originalTopic = originalTopic;
        d.originalPartition = originalPartition;
        d.originalOffset = originalOffset;
        d.messageKey = messageKey;
        d.payload = payload;
        d.exceptionClass = exceptionClass;
        d.exceptionMessage = exceptionMessage;
        d.status = DeadLetterStatus.NEW;
        d.receivedAt = receivedAt;
        return d;
    }

    // ─── Workflow: chỉ message ở trạng thái NEW mới được xử lý, và chỉ một lần ───

    void markReplayed(String actor, Instant now) {
        requireNew("replay");
        if (originalTopic == null) {
            throw new DeadLetterStateException("Dead letter " + id + " has no original topic, cannot replay");
        }
        this.status = DeadLetterStatus.REPLAYED;
        this.resolvedBy = actor;
        this.resolvedAt = now;
    }

    void discard(String actor, String note, Instant now) {
        requireNew("discard");
        this.status = DeadLetterStatus.DISCARDED;
        this.resolvedBy = actor;
        this.resolvedAt = now;
        this.resolutionNote = note;
    }

    private void requireNew(String action) {
        if (status != DeadLetterStatus.NEW) {
            throw new DeadLetterStateException("Cannot " + action + " dead letter " + id + " in status " + status);
        }
    }

    public Long getId() { return id; }
    public String getDltTopic() { return dltTopic; }
    public int getDltPartition() { return dltPartition; }
    public long getDltOffset() { return dltOffset; }
    public String getOriginalTopic() { return originalTopic; }
    public Integer getOriginalPartition() { return originalPartition; }
    public Long getOriginalOffset() { return originalOffset; }
    public String getMessageKey() { return messageKey; }
    public String getPayload() { return payload; }
    public String getExceptionClass() { return exceptionClass; }
    public String getExceptionMessage() { return exceptionMessage; }
    public DeadLetterStatus getStatus() { return status; }
    public Instant getReceivedAt() { return receivedAt; }
    public String getResolvedBy() { return resolvedBy; }
    public Instant getResolvedAt() { return resolvedAt; }
    public String getResolutionNote() { return resolutionNote; }
}
