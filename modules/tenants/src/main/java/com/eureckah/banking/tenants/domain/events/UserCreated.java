package com.eureckah.banking.tenants.domain.events;

import com.eureckah.banking.tenants.domain.value.UserSnapshot;

public record UserCreated(UserSnapshot user) implements TenantEvent {}
