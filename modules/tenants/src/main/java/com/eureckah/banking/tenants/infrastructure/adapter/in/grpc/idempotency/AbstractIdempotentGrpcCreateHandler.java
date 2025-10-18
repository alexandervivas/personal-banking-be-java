package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc.idempotency;

import com.eureckah.banking.tenants.application.exceptions.FailedCommandException;
import com.eureckah.banking.tenants.application.exceptions.InvalidCommandException;
import com.eureckah.banking.tenants.infrastructure.idempotency.IdempotentOperationCoordinator;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Template for idempotent gRPC "create" operations. Subclasses implement resource-specific
 * validation, mutation, and response mapping.
 *
 * @param <Req> gRPC request type
 * @param <Resp> gRPC response type
 */
public abstract class AbstractIdempotentGrpcCreateHandler<Req, Resp> {

    private final IdempotentOperationCoordinator coordinator;

    protected AbstractIdempotentGrpcCreateHandler(IdempotentOperationCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    /** Route identifier used for idempotency scoping, e.g. "tenants.createTenant.v1". */
    protected abstract String route();

    /** Execute the creation use case and return the created resource id when successful. */
    protected abstract Optional<UUID> execute(UUID userId, Req request)
            throws InvalidCommandException, FailedCommandException;

    /** Build and send the success response to the client for the given resource id. */
    protected abstract void sendSuccess(UUID resourceId, StreamObserver<Resp> responseObserver);

    /**
     * Validate preconditions (e.g., ensure user exists). If validation fails, this method must send
     * the appropriate error via responseObserver and return Optional.empty(). If validation passes,
     * returns a context object or just user id again if not needed.
     */
    protected boolean validate(UUID userId, Req request, StreamObserver<Resp> responseObserver) {
        // Default: no extra validation
        return true;
    }

    public void handle(Req request, StreamObserver<Resp> responseObserver) {
        try {
            String idempotencyKey = IdempotencyContext.idempotencyKey();
            String route = route();
            UUID userId = UUID.fromString(IdempotencyContext.userId());

            // 1) Replay if we already have a final response
            Optional<UUID> replayId = coordinator.findFinal(route, idempotencyKey, userId);
            if (replayId.isPresent()) {
                sendSuccess(replayId.get(), responseObserver);
                return;
            }

            // 1b) Replay terminal failure if present
            Optional<Integer> failure = coordinator.findFailure(route, idempotencyKey, userId);
            if (failure.isPresent()) {
                responseObserver.onError(mapHttpStatusToGrpc(failure.get()).asRuntimeException());
                return;
            }

            // 2) Preconditions
            if (!validate(userId, request, responseObserver)) {
                return; // validation already reported error
            }

            // 3) Acquire-or-wait idempotent execution
            var outcome =
                    coordinator.tryAcquireOrWait(
                            route, idempotencyKey, userId, Duration.ofSeconds(5));
            switch (outcome.type) {
                case REPLAY -> {
                    sendSuccess(outcome.replayId, responseObserver);
                    return;
                }
                case FAILURE -> {
                    responseObserver.onError(
                            mapHttpStatusToGrpc(outcome.failureStatus).asRuntimeException());
                    return;
                }
                case TIMEOUT -> {
                    responseObserver.onError(
                            Status.ABORTED
                                    .withDescription(
                                            "Request with the same idempotency key is in progress;"
                                                    + " retry later")
                                    .asRuntimeException());
                    return;
                }
                case ACQUIRED -> {
                    /* proceed */
                }
            }

            // 4) Execute mutation (we won the placeholder race)
            Optional<UUID> createdId = execute(userId, request);
            if (createdId.isEmpty()) {
                // mark terminal failure so subsequent calls with same key replay the error
                coordinator.markFailed(route, idempotencyKey, userId, 412);
                responseObserver.onError(
                        Status.FAILED_PRECONDITION
                                .withDescription("Failed to create resource")
                                .asRuntimeException());
                return;
            }

            // 5) Persist final response
            coordinator.markCreated(route, idempotencyKey, userId, createdId.get());

            // 6) Send success
            sendSuccess(createdId.get(), responseObserver);
        } catch (IllegalArgumentException ex) {
            releaseIdempotencyKeyWithStatusCode(400);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("x-user-id must be provided")
                            .asRuntimeException());
        } catch (InvalidCommandException ex) {
            releaseIdempotencyKeyWithStatusCode(422);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Invalid command: " + ex.getReason())
                            .asRuntimeException());
        } catch (FailedCommandException ex) {
            // Persist terminal failure for operational errors too
            releaseIdempotencyKeyWithStatusCode(500);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Failed to process command")
                            .asRuntimeException());
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.UNKNOWN.withDescription("Unexpected error").asRuntimeException());
        }
    }

    private void releaseIdempotencyKeyWithStatusCode(int statusCode) {
        // Persist terminal failure so the idempotency key is released and future calls replay
        // deterministically
        try {
            String key = IdempotencyContext.idempotencyKey();
            UUID uid = UUID.fromString(IdempotencyContext.userId());
            coordinator.markFailed(route(), key, uid, statusCode);
        } catch (Exception ignore) {
            // ignore any issues obtaining context or persisting failure
        }
    }

    private static Status mapHttpStatusToGrpc(int code) {
        return switch (code) {
            case 400, 422 -> Status.INVALID_ARGUMENT;
            case 401 -> Status.UNAUTHENTICATED;
            case 403 -> Status.PERMISSION_DENIED;
            case 404 -> Status.NOT_FOUND;
            case 409 -> Status.ALREADY_EXISTS;
            case 412 -> Status.FAILED_PRECONDITION;
            case 429 -> Status.RESOURCE_EXHAUSTED;
            case 499 -> Status.CANCELLED;
            case 500 -> Status.INTERNAL;
            case 503 -> Status.UNAVAILABLE;
            default -> Status.UNKNOWN;
        };
    }
}
