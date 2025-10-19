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
import com.eureckah.banking.tenants.domain.values.TenantSnapshot;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.Optional;
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
    public Optional<UUID> handle(CreateTenantCommand command) {
        Tenant tenant = createTenant(command);
        TenantView tenantView = TenantMapper.toView(tenant, command.user().getId());

        createTenantOwner(command, tenant);
        TenantSnapshot snapshot =
                new TenantSnapshot(tenant.getId(), tenant.getName(), command.user().getId());
        notifyTenantCreation(snapshot);

        return Optional.of(tenant.getId());
    }

    private void notifyTenantCreation(TenantSnapshot snapshot) {
        eventsPublisher.publishTenantEvent(new TenantCreated(snapshot));
    }

    private Tenant createTenant(CreateTenantCommand command) {
        Tenant tenant = Tenant.create(command.name());
        UUID id = tenantRepository.save(tenant);
        return tenant.updateId(id);
    }

    private void createTenantOwner(CreateTenantCommand command, Tenant tenant) {
        Profile profile = Profile.create(tenant, command.user(), Role.OWNER);
        profileRepository.save(profile);
    }
}
