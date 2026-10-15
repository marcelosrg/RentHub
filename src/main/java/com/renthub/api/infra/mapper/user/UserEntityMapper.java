package com.renthub.api.infra.mapper.user;

import com.renthub.api.core.domain.user.User;
import com.renthub.api.infra.persistence.user.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {
    public User toDomain(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getAvatarUrl(),
                userEntity.getAuthProvider(),
                userEntity.getProviderId(),
                userEntity.getCreatedAt(),
                userEntity.getUpdatedAt()
        );
    }

    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.id())
                .name(user.name())
                .email(user.email())
                .avatarUrl(user.avatarUrl())
                .authProvider(user.authProvider())
                .providerId(user.providerId())
                .createdAt(user.createdAt())
                .updatedAt(user.updatedAt())
                .build();
    }
}
