package com.eureckah.banking.tenants.application.dto;

import java.util.UUID;

public record TenantView(UUID id, String name, UUID ownerId) {}
