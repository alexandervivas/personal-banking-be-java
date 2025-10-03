package com.eureckah.banking.tenants.application.queries;

public interface QueryHandler<Query, Response> {
    Response handle(Query query);
}
