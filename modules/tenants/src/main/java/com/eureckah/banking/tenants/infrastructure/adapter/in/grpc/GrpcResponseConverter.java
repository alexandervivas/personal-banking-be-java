package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc;

import com.eureckah.banking.tenants.proto.v1.CreateTenantResponse;
import com.eureckah.banking.users.proto.v1.CreateUserResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GrpcResponseConverter {

    public static CreateTenantResponse toCreateTenantResponse(UUID tenantId) {
        return CreateTenantResponse.newBuilder().setTenantId(tenantId.toString()).build();
    }

    public static CreateUserResponse toCreateUserResponse(UUID userId) {
        return CreateUserResponse.newBuilder().setUserId(userId.toString()).build();
    }
}
