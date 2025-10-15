package com.eureckah.banking.tenants.application.queries;

import java.util.UUID;

public record GetUserQuery(UUID id) implements Query {}
