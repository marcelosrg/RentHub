package com.renthub.api.infra.dtos.user;

import com.renthub.api.core.enums.AuthProvider;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse (UUID id, String name,
                       String email,
                       String avatarUrl,
                       AuthProvider authProvider,
                       String providerId,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
}
