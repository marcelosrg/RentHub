package com.renthub.api.core.usecases.user;

import com.renthub.api.core.domain.user.User;

import java.util.Optional;
import java.util.UUID;

public interface GetUserByIdUseCase {
    User execute(UUID userId);
}
