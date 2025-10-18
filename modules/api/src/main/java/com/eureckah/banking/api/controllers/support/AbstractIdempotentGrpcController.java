package com.eureckah.banking.api.controllers.support;

import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;

/**
 * Base controller providing a Template Method-style helper for idempotent HTTP -> gRPC calls.
 *
 * <p>Responsibilities moved here so concrete controllers focus on request/response mapping only: -
 * Build and attach standard per-call gRPC metadata (idempotency-key, x-user-id) - Provide a safe
 * way to attach headers to a given gRPC stub
 */
public abstract class AbstractIdempotentGrpcController {

    protected static final Metadata.Key<String> IDEMPOTENCY_KEY =
            Metadata.Key.of("idempotency-key", Metadata.ASCII_STRING_MARSHALLER);
    protected static final Metadata.Key<String> USER_ID_KEY =
            Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);

    /** Build the standard metadata required for idempotent calls. */
    protected Metadata buildIdempotencyMetadata(String userId, String idempotencyKey) {
        Metadata md = new Metadata();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            md.put(IDEMPOTENCY_KEY, idempotencyKey);
        }
        if (userId != null && !userId.isBlank()) {
            md.put(USER_ID_KEY, userId);
        }
        return md;
    }

    /**
     * Attach headers to the given stub. If the stub implementation returns null (e.g. a mock in
     * tests), fall back to the original stub to avoid NPEs.
     */
    protected <S extends AbstractStub<S>> S attachHeaders(S stub, Metadata headers) {
        ClientInterceptor interceptor = MetadataUtils.newAttachHeadersInterceptor(headers);
        S with = stub.withInterceptors(interceptor);
        return with != null ? with : stub;
    }
}
