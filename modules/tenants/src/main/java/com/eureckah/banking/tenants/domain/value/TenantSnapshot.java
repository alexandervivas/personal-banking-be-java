package com.eureckah.banking.tenants.domain.value;

import java.util.UUID;

/**
 * Snapshot of a Tenant aggregate required by domain events. Located in the domain/value package to
 * avoid depending on application layer DTOs.
 */
public record TenantSnapshot(UUID id, String name, UUID ownerId) {}
