package com.eureckah.banking.shared.application.port.in;

import com.eureckah.banking.shared.application.commands.Command;

import java.util.Optional;
import java.util.UUID;

public interface CommandUseCase<C extends Command> {
    Optional<UUID> handle(C command);
}
