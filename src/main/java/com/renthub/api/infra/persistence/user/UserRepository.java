package com.renthub.api.infra.persistence.user;

import com.renthub.api.core.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);
    boolean existsByAuthProviderAndProviderId(AuthProvider authProvider, String authProviderId);
}
