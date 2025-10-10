package com.eureckah.banking.tenants.application.commands;

public record CreateTenantCommand(String name) {
    public CreateTenantCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
