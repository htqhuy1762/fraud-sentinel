package com.fraudsentinel.scoring.api;

import com.fraudsentinel.scoring.alert.AlertNotFoundException;
import com.fraudsentinel.scoring.alert.AlertStateException;
import com.fraudsentinel.scoring.alert.NotAssigneeException;
import com.fraudsentinel.scoring.messaging.KafkaPublishException;
import com.fraudsentinel.scoring.rule.RuleNotFoundException;
import com.fraudsentinel.scoring.rule.StaleRuleConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(KafkaPublishException.class)
    ProblemDetail onPublishFailure(KafkaPublishException e) {
        log.error("Cannot record decision, rejecting request", e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Fraud decision could not be recorded. Retry later or apply the fallback policy.");
        problem.setTitle("Decision not recorded");
        return problem;
    }

    @ExceptionHandler(AlertNotFoundException.class)
    ProblemDetail onNotFound(AlertNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(AlertStateException.class)
    ProblemDetail onState(AlertStateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(NotAssigneeException.class)
    ProblemDetail onNotAssignee(NotAssigneeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail onConcurrentUpdate(ObjectOptimisticLockingFailureException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The alert was changed by someone else. Reload and try again.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail onBadArgument(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(RuleNotFoundException.class)
    ProblemDetail onRuleNotFound(RuleNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(StaleRuleConfigException.class)
    ProblemDetail onStaleRule(StaleRuleConfigException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }
}