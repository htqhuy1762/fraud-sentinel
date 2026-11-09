package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.common.ForbiddenException;

public class NotAssigneeException extends ForbiddenException {
    public NotAssigneeException(String analyst, String assignee) {
        super("Alert is assigned to " + assignee + ", not " + analyst);
    }
}