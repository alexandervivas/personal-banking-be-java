package com.eureckah.banking.accounts.domain.model;

import com.eureckah.banking.shared.model.CurrencyCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public final class Account {
    private final UUID id;
    private final UUID tenantId;
    private final String name;
    private final AccountType type;
    private final CurrencyCode currency;
}
