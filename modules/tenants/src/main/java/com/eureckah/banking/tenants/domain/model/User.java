package com.eureckah.banking.tenants.domain.model;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {

    private UUID id;

    @NonNull private String name;

    @NonNull private String email;
}
