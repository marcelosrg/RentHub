package com.renthub.api.core.usecases.user;

import com.renthub.api.core.domain.user.User;

public interface FindOrCreateUserUseCase {
    User execute(User user);
}
