package com.eureckah.banking.tenants.infrastructure.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.idempotency.IdempotencyRecordJpaEntity;
import com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.idempotency.IdempotencyRecordJpaRepository;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

class IdempotencyServiceTest {

    private IdempotencyRecordJpaRepository repository;
    private IdempotencyService service;

    @BeforeEach
    void setup() {
        repository = mock(IdempotencyRecordJpaRepository.class);
        service = new IdempotencyService(repository);
    }

    @Test
    void createPlaceholder_returnsEmptyWhenDuplicateKeyViolation() {
        when(repository.save(any(IdempotencyRecordJpaEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate", duplicateConstraint()));

        Optional<IdempotencyRecordJpaEntity> result =
                service.createPlaceholder("route", "key", UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void createPlaceholder_propagatesWhenNotDuplicateKeyViolation() {
        when(repository.save(any(IdempotencyRecordJpaEntity.class)))
                .thenThrow(new DataIntegrityViolationException("other", otherConstraint()));

        assertThatThrownBy(() -> service.createPlaceholder("route", "key", UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("other");
    }

    private ConstraintViolationException duplicateConstraint() {
        return new ConstraintViolationException("duplicate", sqlException("23505"), "uk_idempotency");
    }

    private ConstraintViolationException otherConstraint() {
        return new ConstraintViolationException("other", sqlException("23514"), "chk_length");
    }

    private SQLException sqlException(String sqlState) {
        return new SQLException("constraint", "state", sqlState);
    }
}
