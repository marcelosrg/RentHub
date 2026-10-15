package com.renthub.api.infra.dtos.user;

import com.renthub.api.core.enums.AuthProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(@NotBlank @Size(max = 150) String name,
                                @NotBlank @Email @Size(max = 255) String email,
                                @Size(max = 500) String avatarUrl,
                                @NotNull AuthProvider authProvider,
                                @NotBlank @Size(max = 255) String providerId) {
}
