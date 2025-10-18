package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRecordJpaRepository
        extends JpaRepository<IdempotencyRecordJpaEntity, UUID> {
    Optional<IdempotencyRecordJpaEntity> findByRouteAndKey(String route, String key);
}
