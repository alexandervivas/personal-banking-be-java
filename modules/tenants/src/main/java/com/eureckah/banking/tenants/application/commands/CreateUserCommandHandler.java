package com.eureckah.banking.tenants.application.commands;

import com.eureckah.banking.tenants.application.dto.UserView;
import com.eureckah.banking.tenants.application.mappers.UserMapper;
import com.eureckah.banking.tenants.application.port.in.CreateUserUseCase;
import com.eureckah.banking.tenants.application.port.out.messaging.TenantEventsPublisher;
import com.eureckah.banking.tenants.application.port.out.storage.UserRepository;
import com.eureckah.banking.tenants.domain.events.UserCreated;
import com.eureckah.banking.tenants.domain.model.User;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CreateUserCommandHandler implements CreateUserUseCase {
    private final UserRepository userRepository;
    private final TenantEventsPublisher eventsPublisher;

    public CreateUserCommandHandler(
            UserRepository userRepository, TenantEventsPublisher eventsPublisher) {
        this.userRepository = userRepository;
        this.eventsPublisher = eventsPublisher;
    }

    @Override
    @Transactional
    public Optional<UUID> handle(CreateUserCommand command) {
        User user = createUser(command);
        UserView userView = UserMapper.toView(user);
        notifyUserCreation(userView);
        return Optional.of(user.getId());
    }

    private void notifyUserCreation(UserView userView) {
        eventsPublisher.publishTenantEvent(new UserCreated(userView));
    }

    private User createUser(CreateUserCommand command) {
        User user = User.create(command.name(), command.email());
        UUID id = userRepository.save(user);
        return user.updateId(id);
    }
}
