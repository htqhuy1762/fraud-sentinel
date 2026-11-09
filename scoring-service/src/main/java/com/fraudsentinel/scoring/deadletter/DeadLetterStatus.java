package com.fraudsentinel.scoring.deadletter;

public enum DeadLetterStatus {
    /** Mới vào, chưa ai xử lý. */
    NEW,
    /** Đã gửi lại topic gốc sau khi nguyên nhân được khắc phục. */
    REPLAYED,
    /** Người vận hành quyết định bỏ qua, có ghi lý do. */
    DISCARDED
}
