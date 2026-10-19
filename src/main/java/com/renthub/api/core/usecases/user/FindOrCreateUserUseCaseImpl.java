package com.renthub.api.core.usecases.user;

import com.renthub.api.core.domain.user.User;
import com.renthub.api.core.gateway.user.UserGateway;

public class FindOrCreateUserUseCaseImpl implements FindOrCreateUserUseCase {

    private final UserGateway userGateway;
    private final CreateUserUseCase createUserUseCase;

    public FindOrCreateUserUseCaseImpl(UserGateway userGateway, CreateUserUseCase createUserUseCase){

        this.userGateway = userGateway;
        this.createUserUseCase = createUserUseCase;

    }

    @Override
    public User execute(User user) {
        return userGateway.findByAuthProviderAndProviderId(user.authProvider(), user.providerId())
                .orElseGet(() -> createUserUseCase.execute(user));
    }
}
