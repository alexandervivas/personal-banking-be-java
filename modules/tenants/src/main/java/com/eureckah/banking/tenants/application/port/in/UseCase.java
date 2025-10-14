package com.eureckah.banking.tenants.application.port.in;

import com.eureckah.banking.tenants.application.commands.Command;
import com.eureckah.banking.tenants.application.dto.View;

public interface UseCase<C extends Command, V extends View> {
    V handle(C command);
}
