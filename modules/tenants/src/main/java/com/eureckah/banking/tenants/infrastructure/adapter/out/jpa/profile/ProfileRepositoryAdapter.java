package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.profile;

import com.eureckah.banking.tenants.application.port.out.storage.ProfileRepository;
import com.eureckah.banking.tenants.domain.model.Profile;
import com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.tenant.TenantJpaEntity;
import com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.user.UserJpaEntity;

import jakarta.persistence.EntityManager;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Primary
@Repository
public class ProfileRepositoryAdapter implements ProfileRepository {

    private final ProfileJpaRepository jpa;
    private final EntityManager em;

    public ProfileRepositoryAdapter(ProfileJpaRepository jpa, EntityManager em) {
        this.jpa = jpa;
        this.em = em;
    }

    @Override
    public UUID save(Profile profile) {
        var tenantRef = em.getReference(TenantJpaEntity.class, profile.getTenant().getId());
        var userId = profile.getUser().getId();
        var userRef = em.getReference(UserJpaEntity.class, userId);

        var entity =
                ProfileJpaEntity.builder()
                        .id(profile.getId())
                        .tenant(tenantRef)
                        .user(userRef)
                        .role(profile.getRole())
                        .build();

        return jpa.save(entity).getId();
    }
}
