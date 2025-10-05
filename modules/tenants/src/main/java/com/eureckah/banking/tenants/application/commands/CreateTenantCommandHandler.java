package com.eureckah.banking.tenants.application.commands;

import com.eureckah.banking.tenants.domain.model.TenantId;

import org.springframework.stereotype.Component;

@Component
public class CreateTenantCommandHandler implements CommandHandler<CreateTenantCommand, TenantId> {
    @Override
    public TenantId handle(CreateTenantCommand createTenantCommand) {
        return null;
    }
}
