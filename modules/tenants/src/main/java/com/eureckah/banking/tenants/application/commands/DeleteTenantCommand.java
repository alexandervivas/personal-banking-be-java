package com.eureckah.banking.tenants.application.commands;

import com.eureckah.banking.tenants.domain.TenantId;

public record DeleteTenantCommand(TenantId id) {
}
