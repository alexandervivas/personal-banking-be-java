package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.profile;

import com.eureckah.banking.tenants.domain.model.Profile;
import com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.tenant.TenantJpaMapper;
import com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.user.UserJpaMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileJpaMapper {

    public static Profile toDomain(ProfileJpaEntity entity) {
        return Profile.builder()
                .id(entity.getId())
                .tenant(TenantJpaMapper.toDomain(entity.getTenant()))
                .user(UserJpaMapper.toDomain(entity.getUser()))
                .role(entity.getRole())
                .build();
    }

    public static ProfileJpaEntity toEntity(Profile profile) {
        return ProfileJpaEntity.builder()
                .id(profile.getId())
                .tenant(TenantJpaMapper.toEntity(profile.getTenant()))
                .user(UserJpaMapper.toEntity(profile.getUser()))
                .role(profile.getRole())
                .build();
    }
}
