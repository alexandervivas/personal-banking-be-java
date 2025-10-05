package com.eureckah.banking.tenants.application.commands;

import com.eureckah.banking.tenants.application.exceptions.FailedCommandException;
import com.eureckah.banking.tenants.application.exceptions.InvalidCommandException;
import com.eureckah.banking.tenants.application.ports.repository.TenantRepository;
import com.eureckah.banking.tenants.domain.Tenant;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateTenantCommandHandler implements CommandHandler<CreateTenantCommand, UUID> {
    private final TenantRepository tenantRepository;

    public CreateTenantCommandHandler(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public UUID handle(CreateTenantCommand createTenantCommand)
            throws InvalidCommandException, FailedCommandException {
        if (createTenantCommand == null
                || createTenantCommand.name() == null
                || createTenantCommand.name().isBlank()) {
            throw new InvalidCommandException(
                    CreateTenantCommand.class, "Tenant name must be provided");
        }
        try {
            Tenant tenant = Tenant.builder().name(createTenantCommand.name()).build();
            Tenant saved = tenantRepository.save(tenant);
            if (saved.getId() == null) {
                throw new IllegalStateException("Saved tenant returned without id");
            }
            return saved.getId();
        } catch (Exception exception) {
            throw new FailedCommandException(CreateTenantCommand.class, exception);
        }
    }
}
