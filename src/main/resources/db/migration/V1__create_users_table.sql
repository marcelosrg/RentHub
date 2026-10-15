CREATE TABLE users (
                       id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                       name          VARCHAR(150)  NOT NULL,
                       email         VARCHAR(255)  NOT NULL UNIQUE,
                       avatar_url    VARCHAR(500),
                       auth_provider VARCHAR(20)   NOT NULL,
                       provider_id   VARCHAR(255)  NOT NULL,
                       created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
                       updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),

                       CONSTRAINT uk_users_email           UNIQUE (email),
                       CONSTRAINT uk_users_provider        UNIQUE (auth_provider, provider_id),
                       CONSTRAINT ck_users_auth_provider   CHECK (auth_provider IN ('GOOGLE'))
);