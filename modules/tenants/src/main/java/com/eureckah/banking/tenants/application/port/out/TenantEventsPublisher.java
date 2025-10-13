package com.eureckah.banking.tenants.application.port.out;

import com.eureckah.banking.tenants.domain.events.TenantEvent;

public interface TenantEventsPublisher {

    void publishTenantEvent(TenantEvent event);
}
