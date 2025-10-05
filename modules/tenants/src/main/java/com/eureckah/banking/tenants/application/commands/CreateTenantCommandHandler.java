package com.eureckah.banking.tenants.application.commands;

import com.eureckah.banking.tenants.application.exceptions.FailedCommandException;
import com.eureckah.banking.tenants.application.exceptions.InvalidCommandException;
import com.eureckah.banking.tenants.domain.model.TenantId;

import org.springframework.stereotype.Component;

@Component
public class CreateTenantCommandHandler implements CommandHandler<CreateTenantCommand, TenantId> {
    @Override
    public TenantId handle(CreateTenantCommand createTenantCommand)
            throws InvalidCommandException, FailedCommandException {
        return null;
    }
}
