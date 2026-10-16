package com.fraudsentinel.scoring.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AlertListener {

    private static final Logger log = LoggerFactory.getLogger(AlertListener.class);

    @EventListener
    public void onAlert(RuleAlertEvent event) {
        log.warn("ALERT txn={} account={} decision={} score={} reasons={}",
                event.transaction().txnId(),
                event.transaction().accountId(),
                event.result().decision(),
                event.result().score(),
                event.result().reasons());
    }
}