package com.eureckah.banking.tenants.application.commands;

import com.eureckah.banking.tenants.application.exceptions.FailedCommandException;
import com.eureckah.banking.tenants.application.exceptions.InvalidCommandException;

public interface CommandHandler<Command, Effect> {
    Effect handle(Command command) throws InvalidCommandException, FailedCommandException;
}
