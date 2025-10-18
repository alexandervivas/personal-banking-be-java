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
        TIMEOUT
    }

    public static final class AcquireOutcome {
        public final AcquireOutcomeType type;
        public final UUID replayId; // non-null only for REPLAY

        private AcquireOutcome(AcquireOutcomeType type, UUID replayId) {
            this.type = type;
            this.replayId = replayId;
        }

        public static AcquireOutcome acquired() {
            return new AcquireOutcome(AcquireOutcomeType.ACQUIRED, null);
        }

        public static AcquireOutcome replay(UUID id) {
            return new AcquireOutcome(AcquireOutcomeType.REPLAY, id);
        }

        public static AcquireOutcome timeout() {
            return new AcquireOutcome(AcquireOutcomeType.TIMEOUT, null);
        }
    }

    private final IdempotencyService idempotencyService;

    /** Return previously finalized resource id if present. */
    public Optional<UUID> findFinal(String route, String key) {
        return idempotencyService.findFinalResourceId(route, key);
    }

    /**
     * Try to acquire the mutation via placeholder creation. If another request already owns it,
     * wait up to the provided timeout for a final result and indicate whether to replay or timeout.
     */
    public AcquireOutcome tryAcquireOrWait(
            String route, String key, UUID userId, Duration timeout) {
        var placeholder = idempotencyService.createPlaceholder(route, key, userId);
        if (placeholder.isPresent()) {
            return AcquireOutcome.acquired();
        }
        // another request owns; poll for finalized result up to timeout
        long deadlineNanos = System.nanoTime() + timeout.toNanos();
        do {
            var maybeFinal = idempotencyService.findFinalResourceId(route, key);
            if (maybeFinal.isPresent()) {
                return AcquireOutcome.replay(maybeFinal.get());
            }
            try {
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
        idempotencyService.markCreated(route, key, userId, resourceId);
    }
}
