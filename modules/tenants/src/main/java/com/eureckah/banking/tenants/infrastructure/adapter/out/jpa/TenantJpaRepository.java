package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa;

import com.eureckah.banking.tenants.domain.model.Tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantJpaRepository extends JpaRepository<Tenant, UUID> {}
