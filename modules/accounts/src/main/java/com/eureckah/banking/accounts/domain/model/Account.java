package com.eureckah.banking.accounts.domain.model;

import com.eureckah.banking.shared.model.CurrencyCode;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public final class Account {
    private final UUID id;

    @NonNull private final UUID tenantId;

    @NonNull private final String name;

    @NonNull private final AccountType type;

    @NonNull private final CurrencyCode currency;

    public static Account create(
            UUID tenantId, String name, AccountType type, CurrencyCode currency) {
        return builder().tenantId(tenantId).name(name).type(type).currency(currency).build();
    }

    public Account updateId(UUID id) {
        return builder()
                .id(id)
                .tenantId(this.tenantId)
                .name(this.name)
                .type(this.type)
                .currency(this.currency)
                .build();
    }
}
