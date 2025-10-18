package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.user;

import com.eureckah.banking.tenants.application.port.out.storage.UserRepository;
import com.eureckah.banking.tenants.domain.model.User;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    public UserRepositoryAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findById(UUID id) {
        if (id == null) return Optional.empty();
        return jpa.findById(id).map(UserJpaMapper::toDomain);
    }

    @Override
    public UUID save(User user) {
        var entity = UserJpaMapper.toEntity(user);
        return jpa.save(entity).getId();
    }
}
