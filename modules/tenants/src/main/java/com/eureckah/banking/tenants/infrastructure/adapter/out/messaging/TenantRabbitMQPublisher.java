package com.eureckah.banking.tenants.infrastructure.adapter.out.messaging;

import com.eureckah.banking.tenants.application.port.out.TenantEventsPublisher;
import com.eureckah.banking.tenants.domain.events.TenantEvent;

import org.springframework.stereotype.Service;

@Service
public class TenantRabbitMQPublisher implements TenantEventsPublisher {
    @Override
    public void publishTenantEvent(TenantEvent event) {}
}
