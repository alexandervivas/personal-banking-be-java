package com.eureckah.banking.tenants.domain.events;

import com.eureckah.banking.tenants.application.dto.UserView;

public record UserCreated(UserView userView) implements TenantEvent {}
