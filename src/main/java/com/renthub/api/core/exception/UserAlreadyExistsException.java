package com.renthub.api.core.exception;

public class UserAlreadyExistsException extends AlreadyExistsException  {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
