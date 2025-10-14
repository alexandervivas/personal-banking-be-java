package com.eureckah.banking.tenants.domain.model;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Profile {

    private UUID id;

    @NonNull private Tenant tenant;

    @NonNull private User user;

    @NonNull private Role role;

    public static Profile create(Tenant tenant, User user, Role role) {
        return builder().tenant(tenant).user(user).role(role).build();
    }
}
