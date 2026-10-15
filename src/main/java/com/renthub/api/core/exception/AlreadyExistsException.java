package com.renthub.api.core.exception;

public abstract class AlreadyExistsException extends DomainException  {
    protected AlreadyExistsException(String message) {
        super(message);
    }
}
