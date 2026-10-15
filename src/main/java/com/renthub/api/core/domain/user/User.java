package com.renthub.api.core.domain.user;

import com.renthub.api.core.enums.AuthProvider;

import java.time.Instant;
import java.util.UUID;

public record User(UUID id,
                   String name,
                   String email,
                   String avatarUrl,
                   AuthProvider authProvider,
                   String providerId,
                   Instant createdAt,
                   Instant updatedAt) {
}
