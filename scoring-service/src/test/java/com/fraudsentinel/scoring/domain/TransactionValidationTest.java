package com.fraudsentinel.scoring.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionValidationTest {

    private static jakarta.validation.ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private static Transaction txn(String txnId, String currency, String channel, BigDecimal amount) {
        return new Transaction(txnId, "ACC1", amount, currency, channel, "ACC2", "DEV1", "10.0.0.1", Instant.now());
    }

    private Set<String> invalidFields(Transaction t) {
        return validator.validate(t).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void valid_transaction_passes() {
        assertThat(validator.validate(txn("T1", "VND", "MOBILE", new BigDecimal("75000000")))).isEmpty();
    }

    @Test
    void currency_must_be_a_three_letter_code() {
        assertThat(invalidFields(txn("T1", "VNDX", "MOBILE", BigDecimal.TEN))).containsExactly("currency");
        assertThat(invalidFields(txn("T1", "vnd", "MOBILE", BigDecimal.TEN))).containsExactly("currency");
    }

    @Test
    void fields_longer_than_their_database_columns_are_rejected() {
        assertThat(invalidFields(txn("T".repeat(65), "VND", "MOBILE", BigDecimal.TEN))).containsExactly("txnId");
        assertThat(invalidFields(txn("T1", "VND", "X".repeat(17), BigDecimal.TEN))).containsExactly("channel");
    }

    @Test
    void amount_must_fit_numeric_19_2() {
        assertThat(invalidFields(txn("T1", "VND", "MOBILE", new BigDecimal("1.005")))).containsExactly("amount");
    }
}
