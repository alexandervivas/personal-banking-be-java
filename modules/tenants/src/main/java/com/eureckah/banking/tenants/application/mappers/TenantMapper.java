package com.eureckah.banking.tenants.application.mappers;

import com.eureckah.banking.tenants.application.dto.TenantView;
import com.eureckah.banking.tenants.domain.model.Tenant;

import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class TenantMapper {

    public static TenantView toView(Tenant tenant, UUID ownerId) {
        return new TenantView(tenant.getId(), tenant.getName(), ownerId);
    }
}
