package com.eureckah.banking.tenants.domain.events;

import com.eureckah.banking.tenants.application.dto.TenantView;

public record TenantCreated(TenantView tenant) {}
