package com.eureckah.banking.accounts.domain.values;

import com.eureckah.banking.accounts.domain.model.AccountType;
import com.eureckah.banking.shared.model.CurrencyCode;

import java.util.UUID;

public record AccountSnapshot(
        UUID id, UUID tenantId, AccountType type, String name, CurrencyCode currency) {}
