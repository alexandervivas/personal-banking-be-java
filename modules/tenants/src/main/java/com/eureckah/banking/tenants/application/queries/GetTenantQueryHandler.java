package com.eureckah.banking.tenants.application.queries;

import com.eureckah.banking.tenants.application.dto.TenantView;

import java.util.Optional;

public class GetTenantQueryHandler implements QueryHandler<GetTenantQuery, Optional<TenantView>> {
    @Override
    public Optional<TenantView> handle(GetTenantQuery getTenantQuery) {
        return Optional.empty();
    }
}
