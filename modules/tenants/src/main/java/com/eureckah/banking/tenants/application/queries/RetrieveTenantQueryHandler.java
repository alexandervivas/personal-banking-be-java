package com.eureckah.banking.tenants.application.queries;

import com.eureckah.banking.tenants.application.views.TenantView;

import java.util.Optional;

public class RetrieveTenantQueryHandler
        implements QueryHandler<RetrieveTenantQuery, Optional<TenantView>> {
    @Override
    public Optional<TenantView> handle(RetrieveTenantQuery retrieveTenantQuery) {
        return Optional.empty();
    }
}
