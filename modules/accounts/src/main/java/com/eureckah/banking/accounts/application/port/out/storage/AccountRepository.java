package com.eureckah.banking.accounts.application.port.out.storage;

import com.eureckah.banking.accounts.domain.model.Account;

import java.util.UUID;

public interface AccountRepository {
    UUID save(Account account);
}
