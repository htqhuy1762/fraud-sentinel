package com.fraudsentinel.scoring.alert;

public class NotAssigneeException extends RuntimeException {
    public NotAssigneeException(String analyst, String assignee) {
        super("Alert is assigned to " + assignee + ", not " + analyst);
    }
}