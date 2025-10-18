package com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.profile;

import com.eureckah.banking.tenants.domain.model.Role;
import com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.tenant.TenantJpaEntity;
import com.eureckah.banking.tenants.infrastructure.adapter.out.jpa.user.UserJpaEntity;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Entity
@Table(name = "profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProfileJpaEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantJpaEntity tenant;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserJpaEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
}
