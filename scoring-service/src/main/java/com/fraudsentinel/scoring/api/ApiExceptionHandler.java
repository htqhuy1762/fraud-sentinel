package com.fraudsentinel.scoring.api;

import com.fraudsentinel.scoring.common.ConflictException;
import com.fraudsentinel.scoring.common.ForbiddenException;
import com.fraudsentinel.scoring.common.InvalidInputException;
import com.fraudsentinel.scoring.common.NotFoundException;
import com.fraudsentinel.scoring.messaging.KafkaPublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Ánh xạ lỗi theo LOẠI (not found / conflict / forbidden / invalid input), không theo từng exception.
 * Lớp cha ResponseEntityExceptionHandler lo các lỗi chuẩn của Spring MVC (validate @Valid thất bại,
 * thiếu header, sai kiểu tham số...) và trả về cùng định dạng ProblemDetail (RFC 9457).
 * Exception không được liệt kê ở đây (kể cả IllegalArgumentException do bug) → 500.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail onNotFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail onConflict(ConflictException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    ProblemDetail onForbidden(ForbiddenException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(InvalidInputException.class)
    ProblemDetail onInvalidInput(InvalidInputException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** @Version của bất kỳ entity nào (alert, rule config...) → thông báo chung, không nói riêng "alert". */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail onConcurrentUpdate(ObjectOptimisticLockingFailureException e) {
        log.info("Optimistic lock conflict on {}", e.getPersistentClassName());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The resource was changed by someone else. Reload and try again.");
    }

    @ExceptionHandler(KafkaPublishException.class)
    ProblemDetail onPublishFailure(KafkaPublishException e) {
        log.error("Cannot record decision, rejecting request", e);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Fraud decision could not be recorded. Retry later or apply the fallback policy.");
        problem.setTitle("Decision not recorded");
        return problem;
    }
}
