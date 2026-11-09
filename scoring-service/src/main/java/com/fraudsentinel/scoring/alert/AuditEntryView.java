package com.fraudsentinel.scoring.alert;

import java.time.Instant;

/** DTO cho lịch sử thao tác trên alert: API không trả thẳng entity AlertAuditEntry. */
public record AuditEntryView(String action, AlertStatus fromStatus, AlertStatus toStatus,
                             String actor, String note, Instant at) {

    static AuditEntryView of(AlertAuditEntry e) {
        return new AuditEntryView(e.getAction(), e.getFromStatus(), e.getToStatus(),
                e.getActor(), e.getNote(), e.getCreatedAt());
    }
}
