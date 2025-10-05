package com.eureckah.banking.tenants.domain;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NonNull
    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
