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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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