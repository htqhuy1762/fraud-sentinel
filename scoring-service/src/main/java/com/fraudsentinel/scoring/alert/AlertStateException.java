package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.common.ConflictException;

public class AlertStateException extends ConflictException {
    public AlertStateException(String message) {
        super(message);
    }
}