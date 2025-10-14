package com.eureckah.banking.tenants.application.dto;

import java.util.UUID;

public record UserView(UUID id, String name, String email) implements View {}
