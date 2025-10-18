package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.idempotency;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_idem_route_key_user",
                    columnNames = {"route", "key", "user_id"})
        })
public class IdempotencyRecordJpaEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "route", nullable = false, length = 128)
    private String route;

    @Column(name = "key", nullable = false, length = 128)
    private String key;

    @Column(name = "user_id", length = 36)
    private UUID userId;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "resource_id", length = 36)
    private String resourceId;

    @Column(name = "resource_type", length = 32)
    private String resourceType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public boolean hasFinalResponse() {
        return statusCode != null && resourceId != null && !resourceId.isBlank();
    }

    public boolean hasTerminalFailure() {
        return statusCode != null
                && (resourceId == null || resourceId.isBlank())
                && statusCode >= 400;
    }

    public void markCreated(UUID resourceId) {
        this.statusCode = 201;
        this.resourceId = resourceId.toString();
    }

    public void markFailed(int statusCode) {
        this.statusCode = statusCode;
        this.resourceId = null;
    }
}
