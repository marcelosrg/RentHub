package com.renthub.api.infra.config.security;

import com.renthub.api.core.domain.user.User;
import com.renthub.api.core.enums.AuthProvider;
import com.renthub.api.core.usecases.user.FindOrCreateUserUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final FindOrCreateUserUseCase findOrCreateUserUseCase;

    public OAuth2LoginSuccessHandler(FindOrCreateUserUseCase findOrCreateUserUseCase) {
        this.findOrCreateUserUseCase = findOrCreateUserUseCase;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if(!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            throw new IllegalStateException("Expected an OIDC user from Google login");
        }

        User user = findOrCreateUserUseCase.execute(toDomain(oidcUser));

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"userId\":\"" + user.id() + "\"}");
    }


    private User toDomain(OidcUser oidcUser) {
        return new User(
                null,
                oidcUser.getFullName(),
                oidcUser.getEmail(),
                oidcUser.getPicture(),
                AuthProvider.GOOGLE,
                oidcUser.getSubject(),
                null,
                null
        );
    }
}

