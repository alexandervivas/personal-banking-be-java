package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc;

import com.eureckah.banking.tenants.application.commands.CreateTenantCommand;
import com.eureckah.banking.tenants.application.commands.CreateTenantCommandHandler;
import com.eureckah.banking.tenants.application.exceptions.FailedCommandException;
import com.eureckah.banking.tenants.application.exceptions.InvalidCommandException;
import com.eureckah.banking.tenants.proto.CreateTenantRequest;
import com.eureckah.banking.tenants.proto.CreateTenantResponse;
import com.eureckah.banking.tenants.proto.TenantsServiceGrpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import lombok.extern.slf4j.Slf4j;

import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@Slf4j
public class TenantsGrpcService extends TenantsServiceGrpc.TenantsServiceImplBase {

    private final CreateTenantCommandHandler createTenantCommandHandler;

    public TenantsGrpcService(CreateTenantCommandHandler createTenantCommandHandler) {
        this.createTenantCommandHandler = createTenantCommandHandler;
    }

    @Override
    public void createTenant(
            CreateTenantRequest request, StreamObserver<CreateTenantResponse> responseObserver) {
        var command = new CreateTenantCommand(request.getName());
        try {
            UUID tenantId = createTenantCommandHandler.handle(command);

            CreateTenantResponse response =
                    CreateTenantResponse.newBuilder().setTenantId(tenantId.toString()).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (InvalidCommandException ex) {
            log.error(
                    "Invalid create tenant command for name {}: {}",
                    request.getName(),
                    ex.getReason(),
                    ex);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Invalid command: " + ex.getReason())
                            .asRuntimeException());
        } catch (FailedCommandException ex) {
            log.error("Failed to create tenant {}", request.getName(), ex);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Failed to create tenant")
                            .asRuntimeException());
        } catch (Exception ex) {
            log.error("Unexpected error creating tenant {}", request.getName(), ex);
            responseObserver.onError(
                    Status.UNKNOWN.withDescription("Unexpected error").asRuntimeException());
        }
    }
}
