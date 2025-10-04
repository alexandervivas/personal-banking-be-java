package com.eureckah.banking.tenants.application.commands;

public interface CommandHandler<Command, Effect> {
    Effect handle(Command command);
}
