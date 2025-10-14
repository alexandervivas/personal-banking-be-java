package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.profile;

import com.eureckah.banking.tenants.application.port.out.storage.ProfileRepository;
import com.eureckah.banking.tenants.domain.model.Profile;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Primary
@Repository
public class ProfileRepositoryAdapter implements ProfileRepository {

    private final ProfileJpaRepository jpa;

    public ProfileRepositoryAdapter(ProfileJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public UUID save(Profile profile) {
        var entity = ProfileJpaMapper.toEntity(profile);
        return jpa.save(entity).getId();
    }
}
