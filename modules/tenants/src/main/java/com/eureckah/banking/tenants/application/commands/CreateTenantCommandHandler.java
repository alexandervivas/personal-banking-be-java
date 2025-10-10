package com.eureckah.banking.tenants.application.commands;

import com.eureckah.banking.tenants.application.exceptions.FailedCommandException;
import com.eureckah.banking.tenants.application.exceptions.InvalidCommandException;
import com.eureckah.banking.tenants.application.port.out.TenantRepository;
import com.eureckah.banking.tenants.domain.model.Tenant;

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
        tenantNameMustExist(createTenantCommand);
        try {
            Tenant saved =
                    tenantRepository.save(
                            Tenant.builder().name(createTenantCommand.name()).build());
            tenantIdMustNotBeNull(saved);
            return saved.getId();
        } catch (Exception exception) {
            throw new FailedCommandException(CreateTenantCommand.class, exception);
        }
    }

    private static void tenantNameMustExist(CreateTenantCommand createTenantCommand) {
        if (createTenantCommand == null
                || createTenantCommand.name() == null
                || createTenantCommand.name().isBlank()) {
            throw new InvalidCommandException(
                    CreateTenantCommand.class, "Tenant name must be provided");
        }
    }

    private static void tenantIdMustNotBeNull(Tenant saved) {
        if (saved.getId() == null) {
            throw new IllegalStateException("Saved tenant returned without id");
        }
    }
}
