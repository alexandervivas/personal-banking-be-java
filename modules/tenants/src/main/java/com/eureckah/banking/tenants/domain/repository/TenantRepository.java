package com.eureckah.banking.tenants.domain.repository;

import com.eureckah.banking.tenants.domain.model.Tenant;
import com.eureckah.banking.tenants.domain.model.TenantId;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {
    Optional<Tenant> findById(TenantId id);

    Tenant save(Tenant tenant);

    void delete(TenantId id);

    List<Tenant> findAll();
}
