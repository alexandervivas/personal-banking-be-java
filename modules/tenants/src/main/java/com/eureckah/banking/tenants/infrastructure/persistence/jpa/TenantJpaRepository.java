package com.eureckah.banking.tenants.infrastructure.persistence.jpa;

import com.eureckah.banking.tenants.domain.Tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantJpaRepository extends JpaRepository<Tenant, UUID> {}
