package com.eureckah.banking.tenants.application.queries;

import com.eureckah.banking.shared.application.queries.Query;

import java.util.UUID;

public record GetUserQuery(UUID id) implements Query {}
