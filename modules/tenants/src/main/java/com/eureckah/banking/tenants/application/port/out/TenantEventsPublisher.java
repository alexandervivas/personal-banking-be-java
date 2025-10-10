package com.eureckah.banking.tenants.application.port.out;

import com.eureckah.banking.tenants.application.dto.TenantView;

public interface TenantEventsPublisher {

    void publishTenantCreated(TenantView tenant);
}
