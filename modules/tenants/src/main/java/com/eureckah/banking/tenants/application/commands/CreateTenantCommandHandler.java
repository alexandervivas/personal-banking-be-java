package com.eureckah.banking.tenants.application.commands;

import com.eureckah.banking.tenants.application.dto.TenantView;
import com.eureckah.banking.tenants.application.mappers.TenantMapper;
import com.eureckah.banking.tenants.application.port.in.CreateTenantUseCase;
import com.eureckah.banking.tenants.application.port.out.messaging.TenantEventsPublisher;
import com.eureckah.banking.tenants.application.port.out.storage.ProfileRepository;
import com.eureckah.banking.tenants.application.port.out.storage.TenantRepository;
import com.eureckah.banking.tenants.domain.events.TenantCreated;
import com.eureckah.banking.tenants.domain.model.Profile;
import com.eureckah.banking.tenants.domain.model.Role;
import com.eureckah.banking.tenants.domain.model.Tenant;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateTenantCommandHandler implements CreateTenantUseCase {
    private final TenantRepository tenantRepository;
    private final ProfileRepository profileRepository;
    private final TenantEventsPublisher eventsPublisher;

    public CreateTenantCommandHandler(
            TenantRepository tenantRepository,
            ProfileRepository profileRepository,
            TenantEventsPublisher eventsPublisher) {
        this.tenantRepository = tenantRepository;
        this.profileRepository = profileRepository;
        this.eventsPublisher = eventsPublisher;
    }

    @Override
    @Transactional
    public TenantView handle(CreateTenantCommand command) {
        Tenant tenant = createTenant(command);
        UUID ownerId = assignTenantOwner(command, tenant);
        TenantView tenantView = TenantMapper.toView(tenant, ownerId);
        notifyTenantCreation(tenantView);
        return tenantView;
    }

    private void notifyTenantCreation(TenantView tenantView) {
        eventsPublisher.publishTenantEvent(new TenantCreated(tenantView));
    }

    private Tenant createTenant(CreateTenantCommand command) {
        Tenant tenant = Tenant.create(command.name());
        UUID id = tenantRepository.save(tenant);
        return tenant.updateId(id);
    }

    private UUID assignTenantOwner(CreateTenantCommand command, Tenant tenant) {
        Profile profile = Profile.create(tenant, command.user(), Role.OWNER);
        return profileRepository.save(profile);
    }
}
