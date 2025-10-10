package com.eureckah.banking.tenants.application.commands;

import com.eureckah.banking.tenants.application.dto.TenantView;
import com.eureckah.banking.tenants.application.port.in.CreateTenantUseCase;
import com.eureckah.banking.tenants.application.port.out.TenantEventsPublisher;
import com.eureckah.banking.tenants.application.port.out.TenantRepository;
import com.eureckah.banking.tenants.domain.model.Tenant;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateTenantCommandHandler implements CreateTenantUseCase {
    private final TenantRepository repository;
    private final TenantEventsPublisher eventsPublisher;

    public CreateTenantCommandHandler(
            TenantRepository repository, TenantEventsPublisher eventsPublisher) {
        this.repository = repository;
        this.eventsPublisher = eventsPublisher;
    }

    @Override
    @Transactional
    public TenantView handle(CreateTenantCommand command) {
        TenantView tenantView = createTenant(command);
        notifyTenantCreation(tenantView);
        return tenantView;
    }

    private void notifyTenantCreation(TenantView tenantView) {
        eventsPublisher.publishTenantCreated(tenantView);
    }

    private TenantView createTenant(CreateTenantCommand command) {
        Tenant tenant = Tenant.create(command.name());
        UUID id = repository.save(tenant);
        return new TenantView(id, tenant.getName());
    }
}
