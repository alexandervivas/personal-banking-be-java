package com.eureckah.banking.accounts.application.port.out.messaging;

import com.eureckah.banking.accounts.domain.events.AccountEvent;

public interface AccountEventsPublisher {
    void publish(AccountEvent event);
}
