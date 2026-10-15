package com.renthub.api.core.domain.user;

import com.renthub.api.core.enums.AuthProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
    @DisplayName("should normalize email to trimmed lowercase")
    @CsvSource({
            "maria@example.com,         maria@example.com",
            "MARIA@EXAMPLE.COM,         maria@example.com",
            "Maria@Example.Com,         maria@example.com",
            "'  maria@example.com  ',   maria@example.com"
    })
    void shouldNormalizeEmail(String input, String expected) {
        User user = userWithEmail(input);

        assertThat(user.email()).isEqualTo(expected);
    }

    @Test
    @DisplayName("should reject null email")
    void shouldRejectNullEmail() {
        assertThatThrownBy(() -> userWithEmail(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("email is required");
    }

    private static User userWithEmail(String email) {
        return new User(null, "Maria", email, null,
                AuthProvider.GOOGLE, "google-123", null, null);
    }
}