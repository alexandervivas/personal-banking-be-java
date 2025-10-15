package com.eureckah.banking.tenants.application.exceptions;

public class FailedCommandException extends RuntimeException {
    public FailedCommandException(Class<?> commandClass, Exception exception) {
        super(
                String.format(
                        "Command %s failed with exception: %s",
                        commandClass.getCanonicalName(), exception.getMessage()),
                exception);
    }
}
