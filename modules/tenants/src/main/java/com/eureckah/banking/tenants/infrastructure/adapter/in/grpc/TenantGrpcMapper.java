package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc;

import com.eureckah.banking.tenants.proto.v1.CreateTenantResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantGrpcMapper {

    public static CreateTenantResponse toResponse(UUID tenantId) {
        return CreateTenantResponse.newBuilder().setTenantId(tenantId.toString()).build();
    }
}
