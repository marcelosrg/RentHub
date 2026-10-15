package com.renthub.api.infra.gateway.user;

import com.renthub.api.core.domain.user.User;
import com.renthub.api.core.enums.AuthProvider;
import com.renthub.api.core.gateway.user.UserGateway;
import com.renthub.api.infra.mapper.user.UserEntityMapper;
import com.renthub.api.infra.persistence.user.UserEntity;
import com.renthub.api.infra.persistence.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryGateway implements UserGateway {

    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;
    public UserRepositoryGateway(UserRepository userRepository,
                                 UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.userEntityMapper = userEntityMapper;
    }

    @Override
    @Transactional
    public User create(User user) {
        UserEntity userEntity = userEntityMapper.toEntity(user);
        UserEntity userEntitySaved = userRepository.save(userEntity);

        return userEntityMapper.toDomain(userEntitySaved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAuthProviderAndProviderId(AuthProvider authProvider, String authProviderId) {
        return userRepository.existsByAuthProviderAndProviderId(authProvider, authProviderId);
    }


}
