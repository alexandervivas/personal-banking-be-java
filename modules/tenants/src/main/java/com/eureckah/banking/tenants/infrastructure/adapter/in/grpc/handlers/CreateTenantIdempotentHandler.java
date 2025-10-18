package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc.handlers;

import com.eureckah.banking.tenants.application.commands.CreateTenantCommand;
import com.eureckah.banking.tenants.application.port.in.CreateTenantUseCase;
import com.eureckah.banking.tenants.application.port.in.GetUserUseCase;
import com.eureckah.banking.tenants.application.queries.GetUserQuery;
import com.eureckah.banking.tenants.domain.model.User;
import com.eureckah.banking.tenants.infrastructure.adapter.in.grpc.GrpcResponseConverter;
import com.eureckah.banking.tenants.infrastructure.adapter.in.grpc.idempotency.AbstractIdempotentGrpcCreateHandler;
import com.eureckah.banking.tenants.infrastructure.idempotency.IdempotentOperationCoordinator;
import com.eureckah.banking.tenants.proto.v1.CreateTenantRequest;
import com.eureckah.banking.tenants.proto.v1.CreateTenantResponse;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CreateTenantIdempotentHandler
        extends AbstractIdempotentGrpcCreateHandler<CreateTenantRequest, CreateTenantResponse> {

    private final CreateTenantUseCase createTenantUseCase;
    private final GetUserUseCase getUserUseCase;

    public CreateTenantIdempotentHandler(
            IdempotentOperationCoordinator coordinator,
            CreateTenantUseCase createTenantUseCase,
            GetUserUseCase getUserUseCase) {
        super(coordinator);
        this.createTenantUseCase = createTenantUseCase;
        this.getUserUseCase = getUserUseCase;
    }

    @Override
    protected String route() {
        return "tenants.createTenant.v1";
    }

    @Override
    protected boolean validate(
            UUID userId,
            CreateTenantRequest request,
            StreamObserver<CreateTenantResponse> responseObserver) {
        var user = getUserUseCase.handle(new GetUserQuery(userId));
        if (user.isEmpty()) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION
                            .withDescription("User with id " + userId + " not found")
                            .asRuntimeException());
            return false;
        }
        return true;
    }

    @Override
    protected Optional<UUID> execute(UUID userId, CreateTenantRequest request) {
        // We have validated the user exists; fetch again for building the command.
        User user = getUserUseCase.handle(new GetUserQuery(userId)).orElse(null);
        if (user == null) return Optional.empty();
        var cmd = new CreateTenantCommand(request.getName(), user);
        return createTenantUseCase.handle(cmd);
    }

    @Override
    protected void sendSuccess(
            UUID resourceId, StreamObserver<CreateTenantResponse> responseObserver) {
        var response = GrpcResponseConverter.toCreateTenantResponse(resourceId);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
