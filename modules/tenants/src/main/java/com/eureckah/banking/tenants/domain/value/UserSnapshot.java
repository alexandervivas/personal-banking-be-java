package com.eureckah.banking.tenants.domain.value;

import java.util.UUID;

/**
 * Snapshot of a User aggregate required by domain events. Located in the domain/value package to
 * avoid depending on application layer DTOs.
 */
public record UserSnapshot(UUID id, String name, String email) {}
