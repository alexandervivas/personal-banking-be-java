package com.eureckah.banking.tenants.infrastructure.persistence;

import com.eureckah.banking.tenants.application.ports.repository.TenantRepository;
import com.eureckah.banking.tenants.domain.Tenant;
import com.eureckah.banking.tenants.infrastructure.persistence.jpa.TenantJpaRepository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class DelegatingTenantRepository implements TenantRepository {

    private final TenantJpaRepository delegate;

    public DelegatingTenantRepository(TenantJpaRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<Tenant> findById(UUID id) {
        if (id == null) return Optional.empty();
        return delegate.findById(id);
    }

    @Override
    public Tenant save(Tenant tenant) {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant must not be null");
        }
        return delegate.save(tenant);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) return;
        delegate.deleteById(id);
    }

    @Override
    public List<Tenant> findAll() {
        return delegate.findAll().stream().toList();
    }
}
