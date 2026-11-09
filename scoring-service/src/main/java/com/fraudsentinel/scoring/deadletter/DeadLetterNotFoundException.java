package com.fraudsentinel.scoring.deadletter;

import com.fraudsentinel.scoring.common.NotFoundException;

public class DeadLetterNotFoundException extends NotFoundException {
    public DeadLetterNotFoundException(Long id) {
        super("Dead letter " + id + " not found");
    }
}
