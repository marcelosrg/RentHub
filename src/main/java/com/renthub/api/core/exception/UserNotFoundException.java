package com.renthub.api.core.exception;

import java.util.UUID;

public class UserNotFoundException extends NotFoundException  {
    public UserNotFoundException(UUID id) {
        super("User not found: " + id);
    }
}
