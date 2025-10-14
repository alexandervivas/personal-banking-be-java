package com.eureckah.banking.tenants.infrastructure.adapter.in.grpc.responses;

import com.eureckah.banking.tenants.application.dto.UserView;

public record CreateUserResponse(UserView user) {}
