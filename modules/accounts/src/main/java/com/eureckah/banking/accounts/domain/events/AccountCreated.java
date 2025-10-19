package com.eureckah.banking.accounts.domain.events;

import com.eureckah.banking.accounts.domain.values.AccountSnapshot;

public record AccountCreated(AccountSnapshot snapshot) implements AccountEvent {}
