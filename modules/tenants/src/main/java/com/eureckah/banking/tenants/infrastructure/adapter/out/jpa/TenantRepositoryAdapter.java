package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa;

import com.eureckah.banking.tenants.application.port.out.TenantRepository;
import com.eureckah.banking.tenants.domain.model.Tenant;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@Repository
public class TenantRepositoryAdapter implements TenantRepository {

    private final TenantJpaRepository delegate;

    public TenantRepositoryAdapter(TenantJpaRepository delegate) {
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
