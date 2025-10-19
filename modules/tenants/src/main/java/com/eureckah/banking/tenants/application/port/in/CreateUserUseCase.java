package com.eureckah.banking.tenants.application.port.in;

import com.eureckah.banking.shared.application.port.in.CommandUseCase;
import com.eureckah.banking.tenants.application.commands.CreateUserCommand;

import java.util.Optional;
import java.util.UUID;

public interface CreateUserUseCase extends CommandUseCase<CreateUserCommand> {

    Optional<UUID> handle(CreateUserCommand command);
}
