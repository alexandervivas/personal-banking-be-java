package com.eureckah.banking.tenants.application.exceptions;

import lombok.Getter;

@Getter
public class InvalidCommandException extends RuntimeException {
    private final String reason;

    public InvalidCommandException(Class<?> commandClass, String reason) {
        super(
                String.format(
                        "Command %s is invalid due to: %s",
                        commandClass.getCanonicalName(), reason));
        this.reason = reason;
    }
}
