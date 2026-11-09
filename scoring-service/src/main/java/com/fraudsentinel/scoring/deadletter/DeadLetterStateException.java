package com.fraudsentinel.scoring.deadletter;

import com.fraudsentinel.scoring.common.ConflictException;

public class DeadLetterStateException extends ConflictException {
    public DeadLetterStateException(String message) {
        super(message);
    }
}
