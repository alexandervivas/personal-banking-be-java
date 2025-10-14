package com.eureckah.banking.tenants.application.port.in;

import com.eureckah.banking.tenants.application.commands.CreateUserCommand;
import com.eureckah.banking.tenants.application.dto.UserView;

public interface CreateUserUseCase extends CommandUseCase<CreateUserCommand, UserView> {

    UserView handle(CreateUserCommand command);
}
