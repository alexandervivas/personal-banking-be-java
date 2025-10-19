package com.eureckah.banking.tenants.application.commands;

import com.eureckah.banking.shared.application.commands.Command;

public record CreateUserCommand(String name, String email) implements Command {
    public CreateUserCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
    }
}
