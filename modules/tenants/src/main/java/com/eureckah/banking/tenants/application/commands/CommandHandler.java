package com.eureckah.banking.tenants.application.commands;

import java.util.Optional;

public interface CommandHandler<Command, Effect> {
    Effect handle(Command command);
}
