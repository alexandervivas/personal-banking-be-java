package com.eureckah.banking.tenants.application.port.out.storage;

import com.eureckah.banking.tenants.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);

    UUID save(User user);
}
