package com.eureckah.banking.tenants.domain.events;

import com.eureckah.banking.tenants.domain.values.TenantSnapshot;

public record TenantCreated(TenantSnapshot tenant) implements TenantEvent {}
