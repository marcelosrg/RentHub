package com.renthub.api.infra.persistence.user;

import com.renthub.api.core.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name="avatar_url", nullable = true)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name="auth_provider",nullable = false)
    private AuthProvider authProvider;

    @Column(name="provider_id",nullable = false)
    private String providerId;

    @CreationTimestamp
    @Column(name="created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable = false)
    private Instant updatedAt;
}
