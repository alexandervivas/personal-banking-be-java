package com.eureckah.banking.tenants.application.commands;

import com.eureckah.banking.tenants.domain.model.User;

public record CreateTenantCommand(String name, User user) implements Command {
    public CreateTenantCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
    }
}
