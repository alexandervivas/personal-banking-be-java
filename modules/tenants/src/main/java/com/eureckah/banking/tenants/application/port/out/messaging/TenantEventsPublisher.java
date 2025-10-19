package com.eureckah.banking.tenants.application.port.out.messaging;

import com.eureckah.banking.tenants.domain.events.TenantEvent;

public interface TenantEventsPublisher {

    void publish(TenantEvent event);
}
