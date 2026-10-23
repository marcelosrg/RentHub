package com.renthub.api.infra.config.security;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class JwtTokenService {

    private static final String ISSUER = "renthub-api";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;


    public JwtTokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(UUID userId) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.expiration()))
                .build();


        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
