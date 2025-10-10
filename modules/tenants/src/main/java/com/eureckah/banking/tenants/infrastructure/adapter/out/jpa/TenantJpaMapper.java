package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa;

import com.eureckah.banking.tenants.domain.model.Tenant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantJpaMapper {

    public static Tenant toDomain(TenantJpaEntity entity) {
        return Tenant.builder().id(entity.getId()).name(entity.getName()).build();
    }

    public static TenantJpaEntity toEntity(Tenant tenant) {
        return TenantJpaEntity.builder().id(tenant.getId()).name(tenant.getName()).build();
    }
}
