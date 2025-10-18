package com.eureckah.banking.tenants.application.port.in;

import com.eureckah.banking.tenants.application.commands.Command;

import java.util.Optional;
import java.util.UUID;

public interface CommandUseCase<C extends Command> {
    Optional<UUID> handle(C command);
}
