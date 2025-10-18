package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc;

import com.eureckah.banking.tenants.infrastructure.adapter.in.grpc.handlers.CreateTenantIdempotentHandler;
import com.eureckah.banking.tenants.proto.v1.CreateTenantRequest;
import com.eureckah.banking.tenants.proto.v1.CreateTenantResponse;
import com.eureckah.banking.tenants.proto.v1.TenantsServiceGrpc;

import io.grpc.stub.StreamObserver;

import lombok.extern.slf4j.Slf4j;

import org.springframework.grpc.server.service.GrpcService;

@GrpcService(interceptors = {IdempotencyServerInterceptor.class})
@Slf4j
public class TenantsGrpcService extends TenantsServiceGrpc.TenantsServiceImplBase {

    private final CreateTenantIdempotentHandler createTenantHandler;

    public TenantsGrpcService(CreateTenantIdempotentHandler createTenantHandler) {
        this.createTenantHandler = createTenantHandler;
    }

    @Override
    public void createTenant(
            CreateTenantRequest request, StreamObserver<CreateTenantResponse> responseObserver) {
        createTenantHandler.handle(request, responseObserver);
    }
}
