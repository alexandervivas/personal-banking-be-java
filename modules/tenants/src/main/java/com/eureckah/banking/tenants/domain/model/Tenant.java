package com.eureckah.banking.tenants.domain.model;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Tenant {

    private UUID id;

    @NonNull private String name;

    public static Tenant create(String name) {
        return builder().name(name).build();
    }

    public Tenant updateId(UUID id) {
        return builder().id(id).name(this.name).build();
    }
}
