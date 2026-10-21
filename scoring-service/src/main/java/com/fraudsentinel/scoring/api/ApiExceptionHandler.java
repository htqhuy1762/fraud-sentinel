package com.fraudsentinel.scoring.api;

import com.fraudsentinel.scoring.messaging.KafkaPublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
}