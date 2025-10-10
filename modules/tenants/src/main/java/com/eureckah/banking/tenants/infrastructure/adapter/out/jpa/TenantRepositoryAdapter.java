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

    private final TenantJpaRepository jpa;

    public TenantRepositoryAdapter(TenantJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Tenant> findById(UUID id) {
        if (id == null) return Optional.empty();
        return jpa.findById(id).map(TenantJpaMapper::toDomain);
    }

    @Override
    public UUID save(Tenant tenant) {
        var entity = TenantJpaMapper.toEntity(tenant);
        return jpa.save(entity).getId();
    }

    @Override
    public void delete(UUID id) {
        if (id == null) return;
        jpa.deleteById(id);
    }

    @Override
    public List<Tenant> findAll() {
        return jpa.findAll().stream().map(TenantJpaMapper::toDomain).toList();
    }
}
