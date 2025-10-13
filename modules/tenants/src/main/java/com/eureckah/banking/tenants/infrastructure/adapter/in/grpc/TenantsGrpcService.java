package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc;

import com.eureckah.banking.tenants.application.commands.CreateTenantCommand;
import com.eureckah.banking.tenants.application.exceptions.FailedCommandException;
import com.eureckah.banking.tenants.application.exceptions.InvalidCommandException;
import com.eureckah.banking.tenants.application.port.in.CreateTenantUseCase;
import com.eureckah.banking.tenants.proto.v1.CreateTenantRequest;
import com.eureckah.banking.tenants.proto.v1.CreateTenantResponse;
import com.eureckah.banking.tenants.proto.v1.TenantsServiceGrpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import lombok.extern.slf4j.Slf4j;

import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@Slf4j
public class TenantsGrpcService extends TenantsServiceGrpc.TenantsServiceImplBase {

    private final CreateTenantUseCase createTenantUseCase;

    public TenantsGrpcService(CreateTenantUseCase createTenantUseCase) {
        this.createTenantUseCase = createTenantUseCase;
    }

    @Override
    public void createTenant(
            CreateTenantRequest request, StreamObserver<CreateTenantResponse> responseObserver) {
        try {
            UUID userId = getUserId(request, responseObserver);
            if (userId == null) return;

            var command = new CreateTenantCommand(request.getName(), userId);
            var result = createTenantUseCase.handle(command);
            var response = TenantGrpcMapper.toResponse(result.id());

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

    private UUID getUserId(
            CreateTenantRequest request, StreamObserver<CreateTenantResponse> responseObserver) {
        UUID ownerId;
        try {
            ownerId = UUID.fromString(request.getUserId());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("actor_user_id must be a valid UUID")
                            .asRuntimeException());
            return null;
        }
        return ownerId;
    }
}
