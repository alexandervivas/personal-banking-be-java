package com.eureckah.banking.api.bdd;

import com.eureckah.banking.tenants.proto.v1.CreateTenantRequest;
import com.eureckah.banking.tenants.proto.v1.CreateTenantResponse;
import com.eureckah.banking.tenants.proto.v1.TenantsServiceGrpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class FakeTenantsServer {
    private static Server server;
    private static final AtomicReference<CreateTenantRequest> lastCreateRequest =
            new AtomicReference<>();
    public static final int PORT = 19090;

    public static void start() {
        if (server != null) return;
        server =
                ServerBuilder.forPort(PORT)
                        .addService(
                                new TenantsServiceGrpc.TenantsServiceImplBase() {
                                    @Override
                                    public void createTenant(
                                            CreateTenantRequest request,
                                            StreamObserver<CreateTenantResponse> responseObserver) {
                                        lastCreateRequest.set(request);
                                        String tenantId = UUID.randomUUID().toString();
                                        CreateTenantResponse response =
                                                CreateTenantResponse.newBuilder()
                                                        .setTenantId(tenantId)
                                                        .build();
                                        responseObserver.onNext(response);
                                        responseObserver.onCompleted();
                                    }
                                })
                        .build();
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        if (server != null) {
            server.shutdownNow();
            server = null;
        }
    }

    public static CreateTenantRequest getLastCreateRequest() {
        return lastCreateRequest.get();
    }

    public static void clear() {
        lastCreateRequest.set(null);
    }
}
