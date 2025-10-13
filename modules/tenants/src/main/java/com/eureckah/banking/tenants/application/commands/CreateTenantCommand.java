package com.eureckah.banking.tenants.application.commands;

import java.util.UUID;

public record CreateTenantCommand(String name, UUID ownerUserId) {
    public CreateTenantCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (ownerUserId == null) {
            throw new IllegalArgumentException("ownerUserId must not be null");
        }
    }
}
