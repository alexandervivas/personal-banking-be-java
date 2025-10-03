package com.eureckah.banking.tenants.application.commands;

public interface CommandHandler<Command> {
    void handle(Command command);
}
