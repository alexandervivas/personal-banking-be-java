package com.eureckah.banking.tenants.infrastructure.adapter.out.messaging;

import com.eureckah.banking.tenants.application.dto.TenantView;
import com.eureckah.banking.tenants.application.port.out.TenantEventsPublisher;

import org.springframework.stereotype.Service;

@Service
public class TenantRabbitMQPublisher implements TenantEventsPublisher {
    @Override
    public void publishTenantCreated(TenantView tenant) {}
}
