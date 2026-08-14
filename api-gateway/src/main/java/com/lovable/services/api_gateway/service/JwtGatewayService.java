package com.lovable.services.api_gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtGatewayService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    public void validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Refresh tokens must only be usable against /auth/refresh, never as a
        // regular API bearer token.
        if (!"access".equals(claims.get("type", String.class))) {
            throw new JwtException("Not an access token");
        }
    }
}
