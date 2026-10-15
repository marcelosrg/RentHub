package com.renthub.api.infra.mapper.user;

import com.renthub.api.core.domain.user.User;
import com.renthub.api.infra.dtos.user.CreateUserRequest;
import com.renthub.api.infra.dtos.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {
    public User toDomain(CreateUserRequest dto){
        return new User(
                null,
                dto.name(),
                dto.email(),
                dto.avatarUrl(),
                dto.authProvider(),
                dto.providerId(),
                null,
                null
        );
    }

    public UserResponse toDto(User user){
        return new UserResponse(
                user.id(),
                user.name(),
                user.email(),
                user.avatarUrl(),
                user.authProvider(),
                user.providerId(),
                user.createdAt(),
                user.updatedAt()
        );
    }
}
