package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc.responses;

import com.eureckah.banking.tenants.application.dto.TenantView;

public record CreateTenantResponse(TenantView tenant) {}
