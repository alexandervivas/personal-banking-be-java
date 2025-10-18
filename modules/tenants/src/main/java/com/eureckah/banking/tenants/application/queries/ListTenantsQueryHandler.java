package com.eureckah.banking.tenants.application.queries;

import com.eureckah.banking.tenants.application.dto.TenantView;

import java.util.List;

public class ListTenantsQueryHandler implements QueryHandler<ListTenantsQuery, List<TenantView>> {
    @Override
    public List<TenantView> handle(ListTenantsQuery listTenantsQuery) {
        return List.of();
    }
}
