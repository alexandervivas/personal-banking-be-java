package com.eureckah.banking.tenants.application.port.out.storage;

import com.eureckah.banking.tenants.domain.model.Profile;

import java.util.UUID;

public interface ProfileRepository {
    UUID save(Profile profile);
}
