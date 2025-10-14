package com.eureckah.banking.tenants.application.port.in;

import com.eureckah.banking.tenants.application.commands.CreateTenantCommand;
import com.eureckah.banking.tenants.application.dto.TenantView;

public interface CreateTenantUseCase extends CommandUseCase<CreateTenantCommand, TenantView> {

    TenantView handle(CreateTenantCommand command);
}
