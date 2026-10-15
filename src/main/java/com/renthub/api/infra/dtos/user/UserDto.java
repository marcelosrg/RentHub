package com.renthub.api.infra.dtos.user;

import com.renthub.api.core.enums.AuthProvider;

import java.time.Instant;

public record CreateUserDto (String name,
                             String email,
                             String avatarUrl,
                             AuthProvider authProvider,
                             String providerId) {
}
