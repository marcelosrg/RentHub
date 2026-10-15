package com.renthub.api.core.usecases.user;

import com.renthub.api.core.domain.user.User;
import com.renthub.api.core.exception.UserAlreadyExistsException;
import com.renthub.api.core.gateway.user.UserGateway;

public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserGateway userGateway;
    public CreateUserUseCaseImpl(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    @Override
    public User execute(User user) {
        ensureUserDoesNotExist(user);

        return userGateway.create(user);
    }

    private void ensureUserDoesNotExist(User user) {
        if(userGateway.existsByEmail(user.email())){
            throw new UserAlreadyExistsException("Email already in use");
        }

        if(userGateway.existsByAuthProviderAndProviderId(user.authProvider(), user.providerId())){
            throw new UserAlreadyExistsException("User already registered with this provider");
        }
    }
}
