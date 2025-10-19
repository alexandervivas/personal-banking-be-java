package com.eureckah.banking.shared.application.port.in;

import com.eureckah.banking.shared.application.queries.Query;

public interface QueryUseCase<Q extends Query, Result> {
    Result handle(Q query);
}
