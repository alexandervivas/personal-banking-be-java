package com.eureckah.banking.tenants.application.port.in;

import com.eureckah.banking.shared.application.port.in.CommandUseCase;
import com.eureckah.banking.tenants.application.commands.CreateTenantCommand;

import java.util.Optional;
import java.util.UUID;

public interface CreateTenantUseCase extends CommandUseCase<CreateTenantCommand> {

    Optional<UUID> handle(CreateTenantCommand command);
}
