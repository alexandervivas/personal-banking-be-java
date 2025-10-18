package com.eureckah.banking.tenants.application.port.in;

import com.eureckah.banking.tenants.application.queries.Query;

public interface QueryUseCase<Q extends Query, Result> {
    Result handle(Q query);
}
