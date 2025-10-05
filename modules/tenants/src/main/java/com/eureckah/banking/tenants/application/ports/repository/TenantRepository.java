package com.eureckah.banking.tenants.application.ports.repository;

import com.eureckah.banking.tenants.domain.Tenant;
import com.eureckah.banking.tenants.domain.TenantId;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {
    Optional<Tenant> findById(TenantId id);

    Tenant save(Tenant tenant);

    void delete(TenantId id);

    List<Tenant> findAll();
}
