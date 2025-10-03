package com.eureckah.banking.tenants.domain;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {
    Optional<Tenant> findById(TenantId id);
    Tenant save(Tenant tenant);
    void delete(TenantId id);
    List<Tenant> findAll();
}
