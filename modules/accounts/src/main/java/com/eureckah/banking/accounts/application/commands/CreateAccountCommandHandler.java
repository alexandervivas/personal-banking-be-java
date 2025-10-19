package com.eureckah.banking.accounts.application.commands;

import com.eureckah.banking.accounts.application.port.in.CreateAccountUseCase;
import com.eureckah.banking.accounts.application.port.out.messaging.AccountEventsPublisher;
import com.eureckah.banking.accounts.application.port.out.storage.AccountRepository;
import com.eureckah.banking.accounts.domain.events.AccountCreated;
import com.eureckah.banking.accounts.domain.model.Account;
import com.eureckah.banking.accounts.domain.values.AccountSnapshot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CreateAccountCommandHandler implements CreateAccountUseCase {
    private final AccountRepository accountRepository;
    private final AccountEventsPublisher eventsPublisher;

    public CreateAccountCommandHandler(
            AccountRepository accountRepository, AccountEventsPublisher eventsPublisher) {
        this.accountRepository = accountRepository;
        this.eventsPublisher = eventsPublisher;
    }

    @Override
    @Transactional
    public Optional<UUID> handle(CreateAccountCommand command) {
        Account account = createAccount(command);
        publishAccountCreation(account);
        return Optional.of(account.getId());
    }

    private Account createAccount(CreateAccountCommand command) {
        Account account =
                Account.create(
                        command.tenantId(), command.name(), command.type(), command.currency());
        UUID id = accountRepository.save(account);
        return account.updateId(id);
    }

    private void publishAccountCreation(Account account) {
        AccountSnapshot snapshot =
                new AccountSnapshot(
                        account.getId(),
                        account.getTenantId(),
                        account.getType(),
                        account.getName(),
                        account.getCurrency());
        eventsPublisher.publish(new AccountCreated(snapshot));
    }
}
