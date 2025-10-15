package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfileJpaRepository extends JpaRepository<ProfileJpaEntity, UUID> {}
