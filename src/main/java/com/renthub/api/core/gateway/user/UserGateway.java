package com.renthub.api.core.gateway.user;

import com.renthub.api.core.domain.user.User;
import com.renthub.api.core.enums.AuthProvider;

public interface UserGateway {
    User create(User user);

    boolean existsByEmail(String email);
    boolean existsByAuthProviderAndProviderId(AuthProvider authProvider, String providerId );
}
