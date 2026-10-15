package com.renthub.api.core.gateway.user;

import com.renthub.api.core.domain.user.User;

public interface UserGateway {
    User create(User user);
}
