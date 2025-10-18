package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc.idempotency;

import io.grpc.Context;

public final class IdempotencyContext {
    private static final Context.Key<String> IDEMPOTENCY_KEY = Context.key("idempotency-key");
    private static final Context.Key<String> USER_ID_KEY = Context.key("x-user-id");

    private IdempotencyContext() {}

    public static Context withValues(Context base, String idempotencyKey, String userId) {
        Context ctx = base;
        if (idempotencyKey != null) ctx = ctx.withValue(IDEMPOTENCY_KEY, idempotencyKey);
        if (userId != null) ctx = ctx.withValue(USER_ID_KEY, userId);
        return ctx;
    }

    public static String idempotencyKey() {
        return IDEMPOTENCY_KEY.get();
    }

    public static String userId() {
        return USER_ID_KEY.get();
    }
}
