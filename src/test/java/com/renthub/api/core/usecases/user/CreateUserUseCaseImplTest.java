package com.renthub.api.core.usecases.user;

import com.renthub.api.core.domain.user.User;
import com.renthub.api.core.enums.AuthProvider;
import com.renthub.api.core.exception.UserAlreadyExistsException;
import com.renthub.api.core.gateway.user.UserGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseImplTest {

    @Mock
    private UserGateway userGateway;

    private CreateUserUseCaseImpl createUserUseCase;

    @BeforeEach
    void setUp() {
        createUserUseCase = new CreateUserUseCaseImpl(userGateway);
    }

    @Test
    @DisplayName("should create user when email and provider are not registered")
    void shouldCreateUserWhenEmailAndProviderAreNotRegistered() {
        // given
        User newUser = newUser();
        User savedUser = savedFrom(newUser);

        when(userGateway.existsByEmail(newUser.email())).thenReturn(false);
        when(userGateway.existsByAuthProviderAndProviderId(newUser.authProvider(), newUser.providerId()))
                .thenReturn(false);
        when(userGateway.create(newUser)).thenReturn(savedUser);

        // when
        User result = createUserUseCase.execute(newUser);

        // then
        assertThat(result).isEqualTo(savedUser);
        verify(userGateway).create(newUser);
    }

    @Test
    @DisplayName("should throw when email is already in use")
    void shouldThrowWhenEmailIsAlreadyInUse() {
        // given
        User newUser = newUser();
        when(userGateway.existsByEmail(newUser.email())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> createUserUseCase.execute(newUser))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email already in use");

        verify(userGateway, never()).create(any());
    }

    @Test
    @DisplayName("should throw when provider is already registered")
    void shouldThrowWhenProviderIsAlreadyRegistered() {
        // given
        User newUser = newUser();
        when(userGateway.existsByEmail(newUser.email())).thenReturn(false);
        when(userGateway.existsByAuthProviderAndProviderId(newUser.authProvider(), newUser.providerId()))
                .thenReturn(true);

        // when / then
        assertThatThrownBy(() -> createUserUseCase.execute(newUser))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User already registered with this provider");

        verify(userGateway, never()).create(any());
    }

    private static User newUser() {
        return new User(null, "Marina", "marina@example.com", null,
                AuthProvider.GOOGLE, "google-123", null, null);
    }

    private static User savedFrom(User user) {
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), user.name(), user.email(), user.avatarUrl(),
                user.authProvider(), user.providerId(), now, now);
    }
}