package com.renthub.api.infra.beans;

import com.renthub.api.core.gateway.user.UserGateway;
import com.renthub.api.core.usecases.user.CreateUserUseCase;
import com.renthub.api.core.usecases.user.CreateUserUseCaseImpl;
import com.renthub.api.core.usecases.user.FindOrCreateUserUseCase;
import com.renthub.api.core.usecases.user.FindOrCreateUserUseCaseImpl;
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
}
