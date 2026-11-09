package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.common.NotFoundException;

public class AlertNotFoundException extends NotFoundException {
    public AlertNotFoundException(Long id) {
        super("Alert " + id + " not found");
    }
}