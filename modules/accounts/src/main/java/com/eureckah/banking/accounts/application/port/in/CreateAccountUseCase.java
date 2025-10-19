package com.eureckah.banking.accounts.application.port.in;

import com.eureckah.banking.accounts.application.commands.CreateAccountCommand;
import com.eureckah.banking.shared.application.port.in.CommandUseCase;

public interface CreateAccountUseCase extends CommandUseCase<CreateAccountCommand> {}
