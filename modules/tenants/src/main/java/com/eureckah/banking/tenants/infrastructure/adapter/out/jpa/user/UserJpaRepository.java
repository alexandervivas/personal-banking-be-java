package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {}
