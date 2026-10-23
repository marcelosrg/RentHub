package com.renthub.api.core.exception;

public abstract class NotFoundException extends DomainException {
    protected NotFoundException(String message) {
        super(message);
    }
}
