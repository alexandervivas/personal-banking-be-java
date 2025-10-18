package com.eureckah.banking.tenants.application.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.eureckah.banking.tenants.application.port.out.messaging.TenantEventsPublisher;
import com.eureckah.banking.tenants.application.port.out.storage.ProfileRepository;
import com.eureckah.banking.tenants.application.port.out.storage.TenantRepository;
import com.eureckah.banking.tenants.domain.events.TenantCreated;
import com.eureckah.banking.tenants.domain.model.Tenant;
import com.eureckah.banking.tenants.domain.model.User;
import com.eureckah.banking.tenants.domain.value.TenantSnapshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

public class CreateTenantCommandHandlerTest {

    private TenantRepository tenantRepository;
    private ProfileRepository profileRepository;
    private TenantEventsPublisher eventsPublisher;

    private CreateTenantCommandHandler handler;

    @BeforeEach
    void setup() {
        tenantRepository = mock(TenantRepository.class);
        profileRepository = mock(ProfileRepository.class);
        eventsPublisher = mock(TenantEventsPublisher.class);
        handler =
                new CreateTenantCommandHandler(
                        tenantRepository, profileRepository, eventsPublisher);
    }

    @Test
    void handle_creates_tenant_persists_owner_and_publishes_event() {
        // arrange
        UUID generatedId = UUID.randomUUID();
        when(tenantRepository.save(any(Tenant.class))).thenReturn(generatedId);
        User user =
                User.builder()
                        .id(UUID.randomUUID())
                        .name("Alice")
                        .email("alice@example.com")
                        .build();
        CreateTenantCommand command = new CreateTenantCommand("Acme Corp", user);

        // act
        Optional<UUID> result = handler.handle(command);

        // assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(generatedId);

        verify(tenantRepository, times(1)).save(any(Tenant.class));
        verify(profileRepository, times(1)).save(any());

        ArgumentCaptor<TenantCreated> eventCaptor = ArgumentCaptor.forClass(TenantCreated.class);
        verify(eventsPublisher).publishTenantEvent(eventCaptor.capture());
        TenantCreated published = eventCaptor.getValue();
        TenantSnapshot snapshot = published.tenant();
        assertThat(snapshot.id()).isEqualTo(generatedId);
        assertThat(snapshot.ownerId()).isEqualTo(user.getId());
        assertThat(snapshot.name()).isEqualTo("Acme Corp");
    }
}
