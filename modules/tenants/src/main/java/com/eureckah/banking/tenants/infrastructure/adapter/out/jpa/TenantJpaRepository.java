package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantJpaRepository extends JpaRepository<TenantJpaEntity, UUID> {}
