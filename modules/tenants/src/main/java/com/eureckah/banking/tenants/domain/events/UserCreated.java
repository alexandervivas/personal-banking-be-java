package com.eureckah.banking.tenants.domain.events;

import com.eureckah.banking.tenants.domain.values.UserSnapshot;

public record UserCreated(UserSnapshot user) implements TenantEvent {}
