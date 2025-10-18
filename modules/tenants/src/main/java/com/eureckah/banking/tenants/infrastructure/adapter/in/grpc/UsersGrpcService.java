package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc;

import com.eureckah.banking.tenants.application.commands.CreateUserCommand;
import com.eureckah.banking.tenants.application.exceptions.FailedCommandException;
import com.eureckah.banking.tenants.application.exceptions.InvalidCommandException;
import com.eureckah.banking.tenants.application.port.in.CreateUserUseCase;
import com.eureckah.banking.users.proto.v1.CreateUserRequest;
import com.eureckah.banking.users.proto.v1.CreateUserResponse;
import com.eureckah.banking.users.proto.v1.UsersServiceGrpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import lombok.extern.slf4j.Slf4j;

import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@Slf4j
public class UsersGrpcService extends UsersServiceGrpc.UsersServiceImplBase {

    private final CreateUserUseCase createUserUseCase;

    public UsersGrpcService(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    @Override
    public void createUser(
            CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {
        try {

            var command = new CreateUserCommand(request.getName(), request.getEmail());
            var result = createUserUseCase.handle(command);

            if (result.isEmpty()) {
                responseObserver.onError(
                        Status.FAILED_PRECONDITION
                                .withDescription("Failed to create user")
                                .asRuntimeException());
                return;
            }

            var response = GrpcResponseConverter.toCreateUserResponse(result.get());

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (InvalidCommandException ex) {
            log.error(
                    "Invalid create user command for name {}: {}",
                    request.getName(),
                    ex.getReason(),
                    ex);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Invalid command: " + ex.getReason())
                            .asRuntimeException());
        } catch (FailedCommandException ex) {
            log.error("Failed to create user {}", request.getName(), ex);
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Failed to create user").asRuntimeException());
        } catch (Exception ex) {
            log.error("Unexpected error creating user {}", request.getName(), ex);
            responseObserver.onError(
                    Status.UNKNOWN.withDescription("Unexpected error").asRuntimeException());
        }
    }
}
