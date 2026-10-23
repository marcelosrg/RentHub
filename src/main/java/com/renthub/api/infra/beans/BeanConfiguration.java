package com.renthub.api.infra.beans;

import com.renthub.api.core.gateway.user.UserGateway;
import com.renthub.api.core.usecases.user.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CreateUserUseCase createUserUseCase(UserGateway userGateway) {
        return new CreateUserUseCaseImpl(userGateway);
    }

    @Bean
    public FindOrCreateUserUseCase findOrCreateUserUseCase(UserGateway userGateway, CreateUserUseCase createUserUseCase) {
        return new FindOrCreateUserUseCaseImpl(userGateway, createUserUseCase);
    }

    @Bean
    public GetUserByIdUseCase getUserByIdUseCase(UserGateway userGateway) {
        return new GetUserByIdUseCaseImpl(userGateway);
    }
}
