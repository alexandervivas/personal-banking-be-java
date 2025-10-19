package com.eureckah.banking.accounts.application.commands;

import com.eureckah.banking.accounts.domain.model.AccountType;
import com.eureckah.banking.shared.application.commands.Command;
import com.eureckah.banking.shared.model.CurrencyCode;

import java.util.UUID;

public record CreateAccountCommand(
        UUID tenantId, AccountType type, String name, CurrencyCode currency) implements Command {
    public CreateAccountCommand {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("currency must not be null");
        }
    }
}
