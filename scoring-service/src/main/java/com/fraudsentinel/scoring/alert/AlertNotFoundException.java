package com.fraudsentinel.scoring.alert;

public class AlertNotFoundException extends RuntimeException {
    public AlertNotFoundException(Long id) {
        super("Alert " + id + " not found");
    }
}