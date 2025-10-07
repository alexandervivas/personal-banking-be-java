package com.eureckah.banking.tenants.application.ports.repository;

import com.eureckah.banking.tenants.domain.Tenant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository {
    Optional<Tenant> findById(UUID id);

    Tenant save(Tenant tenant);

    void delete(UUID id);

    List<Tenant> findAll();
}
