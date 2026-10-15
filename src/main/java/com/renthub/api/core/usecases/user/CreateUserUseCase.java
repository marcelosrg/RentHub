package com.renthub.api.core.usecases.user;

import com.renthub.api.core.domain.user.User;

public interface CreateUserUseCase {
    User execute(User user);
}
