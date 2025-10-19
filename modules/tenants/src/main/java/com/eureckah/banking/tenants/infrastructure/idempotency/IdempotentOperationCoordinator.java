package com.eureckah.banking.tenants.infrastructure.idempotency;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Coordinates idempotent create-operation workflow: replay detection, placeholder acquire-or-wait,
 * and finalization. Encapsulates waiting logic so higher-level handlers keep a single
 * responsibility (request flow + response mapping).
 */
@Component
@RequiredArgsConstructor
public class IdempotentOperationCoordinator {

    public enum AcquireOutcomeType {
        ACQUIRED,
        REPLAY,
        FAILURE,
        TIMEOUT
    }

    public static final class AcquireOutcome {
        public final AcquireOutcomeType type;
        public final UUID replayId; // non-null only for REPLAY
        public final Integer failureStatus; // non-null only for FAILURE

        private AcquireOutcome(AcquireOutcomeType type, UUID replayId, Integer failureStatus) {
            this.type = type;
            this.replayId = replayId;
            this.failureStatus = failureStatus;
        }

        public static AcquireOutcome acquired() {
            return new AcquireOutcome(AcquireOutcomeType.ACQUIRED, null, null);
        }

        public static AcquireOutcome replay(UUID id) {
            return new AcquireOutcome(AcquireOutcomeType.REPLAY, id, null);
        }

        public static AcquireOutcome failure(int status) {
            return new AcquireOutcome(AcquireOutcomeType.FAILURE, null, status);
        }

        public static AcquireOutcome timeout() {
            return new AcquireOutcome(AcquireOutcomeType.TIMEOUT, null, null);
        }
    }

    private final IdempotencyService idempotencyService;

    /** Return previously finalized resource id if present. */
    public Optional<UUID> findFinal(String route, String key, UUID userId) {
        if (key == null || key.isBlank()) {
            // No idempotency key provided -> behave as non-idempotent: nothing to replay
            return Optional.empty();
        }
        return idempotencyService.findFinalResourceId(route, key, userId);
    }

    /** Return terminal failure status if present. */
    public Optional<Integer> findFailure(String route, String key, UUID userId) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return idempotencyService.findTerminalFailureStatus(route, key, userId);
    }

    /**
     * Try to acquire the mutation via placeholder creation. If another request already owns it,
     * wait up to the provided timeout for a final result and indicate whether to replay or timeout.
     */
    public AcquireOutcome tryAcquireOrWait(
            String route, String key, UUID userId, Duration timeout) {
        // If there is no idempotency key, bypass acquire/wait and proceed normally.
        if (key == null || key.isBlank()) {
            return AcquireOutcome.acquired();
        }
        var placeholder = idempotencyService.createPlaceholder(route, key, userId);
        if (placeholder.isPresent()) {
            return AcquireOutcome.acquired();
        }
        // another request owns; poll for finalized result up to timeout
        long deadlineNanos = System.nanoTime() + timeout.toNanos();
        do {
            var maybeFinal = idempotencyService.findFinalResourceId(route, key, userId);
            if (maybeFinal.isPresent()) {
                return AcquireOutcome.replay(maybeFinal.get());
            }
            var maybeFailure = idempotencyService.findTerminalFailureStatus(route, key, userId);
            if (maybeFailure.isPresent()) {
                return AcquireOutcome.failure(maybeFailure.get());
            }
            try {
                //noinspection BusyWait
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        } while (System.nanoTime() < deadlineNanos);
        return AcquireOutcome.timeout();
    }

    /** Persist the final created resource for future replays. */
    public void markCreated(String route, String key, UUID userId, UUID resourceId) {
        if (key == null || key.isBlank()) {
            // No idempotency key -> nothing to persist
            return;
        }
        idempotencyService.markCreated(route, key, userId, resourceId);
    }

    /** Persist a terminal failure so that subsequent calls replay the error. */
    public void markFailed(String route, String key, UUID userId, int statusCode) {
        if (key == null || key.isBlank()) {
            return;
        }
        idempotencyService.markFailed(route, key, userId, statusCode);
    }
}
