package com.relive27.mfa.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author: ReLive27
 * @date: 2023/1/12 19:01
 */
public final class JwtGenerator implements TokenGenerator<Jwt> {
    private final JwtEncoder jwtEncoder;

    public JwtGenerator(JwtEncoder jwtEncoder) {
        Assert.notNull(jwtEncoder, "jwtEncoder can not be null");
        this.jwtEncoder = jwtEncoder;
    }

    public Jwt generate(Authentication authentication) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(30, ChronoUnit.MINUTES);
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
        claimsBuilder.subject(authentication.getName())
                .issuer("http://127.0.0.1:8080")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .notBefore(issuedAt);

        JwsHeader.Builder headersBuilder = JwsHeader.with(SignatureAlgorithm.RS256);

        JwsHeader headers = headersBuilder.build();
        JwtClaimsSet claims = claimsBuilder.build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(headers, claims));
    }
}
