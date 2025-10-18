package com.eureckah.banking.tenants.infrastructure.idempotency;

import com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.idempotency.IdempotencyRecordJpaEntity;
import com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.idempotency.IdempotencyRecordJpaRepository;

import jakarta.persistence.EntityExistsException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Small service encapsulating persistence operations for idempotency records. Keeps repository
 * usage in one place and exposes intent-centric methods.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRecordJpaRepository repo;

    public Optional<UUID> findFinalResourceId(String route, String key, UUID userId) {
        if (key == null) return Optional.empty();
        return repo.findByRouteAndKeyAndUserId(route, key, userId)
                .filter(IdempotencyRecordJpaEntity::hasFinalResponse)
                .map(r -> UUID.fromString(r.getResourceId()));
    }

    /** Return terminal failure status code if present. */
    public Optional<Integer> findTerminalFailureStatus(String route, String key, UUID userId) {
        if (key == null) return Optional.empty();
        return repo.findByRouteAndKeyAndUserId(route, key, userId)
                .filter(IdempotencyRecordJpaEntity::hasTerminalFailure)
                .map(IdempotencyRecordJpaEntity::getStatusCode);
    }

    /** Try to create a placeholder record; if a duplicate exists, return Optional.empty(). */
    public Optional<IdempotencyRecordJpaEntity> createPlaceholder(
            String route, String key, UUID userId) {
        if (key == null) return Optional.empty();
        try {
            IdempotencyRecordJpaEntity record =
                    IdempotencyRecordJpaEntity.builder()
                            .route(route)
                            .key(key)
                            .userId(userId)
                            .build();
            return Optional.of(repo.save(record));
        } catch (DataIntegrityViolationException | EntityExistsException dup) {
            // Another request inserted first; let caller continue
            return Optional.empty();
        }
    }

    /**
     * Persist the final created resource for replay. Safe if placeholder was not created locally.
     */
    public void markCreated(String route, String key, UUID userId, UUID resourceId) {
        if (key == null) return;
        IdempotencyRecordJpaEntity record =
                repo.findByRouteAndKeyAndUserId(route, key, userId).orElse(null);
        if (record != null) {
            record.markCreated(resourceId);
            repo.save(record);
            return;
        }
        // As a fallback attempt to create a new final record
        try {
            IdempotencyRecordJpaEntity finalRec =
                    IdempotencyRecordJpaEntity.builder()
                            .route(route)
                            .key(key)
                            .userId(userId)
                            .build();
            finalRec.markCreated(resourceId);
            repo.save(finalRec);
        } catch (Exception e) {
            log.warn(
                    "Failed to persist idempotency final response for key {} route {}",
                    key,
                    route,
                    e);
        }
    }

    /** Persist a terminal failure for replaying errors and freeing the key. */
    public void markFailed(String route, String key, UUID userId, int statusCode) {
        if (key == null) return;
        IdempotencyRecordJpaEntity record =
                repo.findByRouteAndKeyAndUserId(route, key, userId).orElse(null);
        if (record != null) {
            record.markFailed(statusCode);
            repo.save(record);
            return;
        }
        try {
            IdempotencyRecordJpaEntity rec =
                    IdempotencyRecordJpaEntity.builder()
                            .route(route)
                            .key(key)
                            .userId(userId)
                            .build();
            rec.markFailed(statusCode);
            repo.save(rec);
        } catch (Exception e) {
            log.warn("Failed to persist idempotency failure for key {} route {}", key, route, e);
        }
    }
}
