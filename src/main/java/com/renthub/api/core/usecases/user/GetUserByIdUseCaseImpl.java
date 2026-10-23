package com.renthub.api.core.usecases.user;

import com.renthub.api.core.domain.user.User;
import com.renthub.api.core.exception.UserNotFoundException;
import com.renthub.api.core.gateway.user.UserGateway;

import java.util.UUID;

public class GetUserByIdUseCaseImpl implements GetUserByIdUseCase{

    private final UserGateway userGateway;
    public GetUserByIdUseCaseImpl(UserGateway userGateway) {
        this.userGateway = userGateway;
    }


    @Override
    public User execute(UUID userId) {
        return userGateway.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}
