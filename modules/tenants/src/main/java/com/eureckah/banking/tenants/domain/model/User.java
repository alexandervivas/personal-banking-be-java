package com.eureckah.banking.tenants.domain.model;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public final class User {

    private UUID id;

    @NonNull private String name;

    @NonNull private String email;

    public static User create(String name, String email) {
        return builder().name(name).email(email).build();
    }

    public User updateId(UUID id) {
        return builder().id(id).name(this.name).email(this.email).build();
    }
}
