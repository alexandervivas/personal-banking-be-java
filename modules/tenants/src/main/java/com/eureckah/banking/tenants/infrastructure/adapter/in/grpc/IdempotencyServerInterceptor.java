package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc;

import com.eureckah.banking.tenants.infrastructure.adapter.in.grpc.idempotency.IdempotencyContext;

import io.grpc.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdempotencyServerInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> IDEM_KEY =
            Metadata.Key.of("idempotency-key", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> USER_ID_KEY =
            Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String idempotencyKey = headers.get(IDEM_KEY);
        String userId = headers.get(USER_ID_KEY);

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // API is supposed to require this; keep server lenient: log warning and proceed.
            log.warn(
                    "Missing idempotency-key metadata for method {}",
                    call.getMethodDescriptor().getFullMethodName());
        }
        if (userId == null || userId.isBlank()) {
            log.debug(
                    "Missing x-user-id metadata for method {}",
                    call.getMethodDescriptor().getFullMethodName());
        }

        Context ctx = IdempotencyContext.withValues(Context.current(), idempotencyKey, userId);
        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
