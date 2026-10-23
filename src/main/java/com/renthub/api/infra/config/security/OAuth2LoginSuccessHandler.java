package com.renthub.api.infra.config.security;

import com.renthub.api.core.domain.user.User;
import com.renthub.api.core.enums.AuthProvider;
import com.renthub.api.core.exception.AlreadyExistsException;
import com.renthub.api.core.usecases.user.FindOrCreateUserUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final FindOrCreateUserUseCase findOrCreateUserUseCase;
    private final JwtTokenService jwtTokenService;
    private final JsonMapper jsonMapper;

    public OAuth2LoginSuccessHandler(FindOrCreateUserUseCase findOrCreateUserUseCase,
                                     JwtTokenService jwtTokenService,
                                     JsonMapper jsonMapper) {
        this.findOrCreateUserUseCase = findOrCreateUserUseCase;
        this.jwtTokenService = jwtTokenService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if(!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            throw new IllegalStateException("Expected an OIDC user from Google login");
        }

        try{
            User user = findOrCreateUserUseCase.execute(toDomain(oidcUser));
            String accessToken = jwtTokenService.generateToken(user.id());
            writeJson(response, HttpStatus.OK, MediaType.APPLICATION_JSON_VALUE, Map.of("accessToken", accessToken, "tokenType", "Bearer"));
        } catch (AlreadyExistsException ex){
            writeJson(response, HttpStatus.CONFLICT, MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    Map.of("title", "conflit",
                            "status", HttpStatus.CONFLICT.value(),
                            "detail", ex.getMessage()));
        }

        User user = findOrCreateUserUseCase.execute(toDomain(oidcUser));

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"userId\":\"" + user.id() + "\"}");
    }


    private void writeJson(HttpServletResponse response,
                           HttpStatus status,
                           String contentType,
                           Object body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(contentType);
        jsonMapper.writeValue(response.getWriter(), body);
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

