package com.renthub.api.core.usecases.user;

import com.renthub.api.core.domain.user.User;
import com.renthub.api.core.enums.AuthProvider;
import com.renthub.api.core.gateway.user.UserGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindOrCreateUserUseCaseImplTest {

    @Mock
    private UserGateway userGateway;

    @Mock
    private CreateUserUseCase createUserUseCase;

    private FindOrCreateUserUseCaseImpl findOrCreateUserUseCase;

    @BeforeEach
    void setUp() {
        findOrCreateUserUseCase = new FindOrCreateUserUseCaseImpl(userGateway, createUserUseCase);
    }

    @Test
    @DisplayName("should return existing user when already registered with provider")
    void shouldReturnExistingUserWhenAlreadyRegistered() {
        // given
        User googleUser = googleUser();
        User existingUser = savedFrom(googleUser);
        when(userGateway.findByAuthProviderAndProviderId(googleUser.authProvider(), googleUser.providerId()))
                .thenReturn(Optional.of(existingUser));

        // when
        User result = findOrCreateUserUseCase.execute(googleUser);

        // then
        assertThat(result).isEqualTo(existingUser);
        verify(createUserUseCase, never()).execute(any());
    }

    @Test
    @DisplayName("should create user when not registered with provider")
    void shouldCreateUserWhenNotRegistered() {
        // given
        User googleUser = googleUser();
        User createdUser = savedFrom(googleUser);
        when(userGateway.findByAuthProviderAndProviderId(googleUser.authProvider(), googleUser.providerId()))
                .thenReturn(Optional.empty());
        when(createUserUseCase.execute(googleUser)).thenReturn(createdUser);

        // when
        User result = findOrCreateUserUseCase.execute(googleUser);

        // then
        assertThat(result).isEqualTo(createdUser);
        verify(createUserUseCase).execute(googleUser);
    }

    private static User googleUser() {
        return new User(null, "Maria", "maria@example.com", "https://example.com/avatar.png",
                AuthProvider.GOOGLE, "google-sub-123", null, null);
    }

    private static User savedFrom(User user) {
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), user.name(), user.email(), user.avatarUrl(),
                user.authProvider(), user.providerId(), now, now);
    }
}