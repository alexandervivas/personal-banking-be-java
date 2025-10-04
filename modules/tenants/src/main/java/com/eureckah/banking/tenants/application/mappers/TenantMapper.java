package com.eureckah.banking.tenants.application.mappers;

import com.eureckah.banking.tenants.application.views.TenantView;
import com.eureckah.banking.tenants.domain.Tenant;

public final class TenantMapper {

    private TenantMapper() {}

    public TenantView toView(Tenant tenant) {
        return new TenantView(tenant.getId().id(), tenant.getName());
    }
}
