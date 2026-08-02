package com.lovable.services.common_lib.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

@Service
public class AuthUtil {

  @Value("${jwt.secretKey}")
  private String jwtSecretKey;

  private SecretKey getSecretKey() {
    return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(JwtUserPrincipal user) {
    return Jwts.builder()
        .subject(user.getUsername())
        .claim("id", user.userId())
            .claim("name", user.name())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 1000 * 600))
        .signWith(getSecretKey())
        .compact();
  }

  public String generateRefreshToken(JwtUserPrincipal user) {
    return Jwts.builder()
        .subject(user.getUsername())
        .claim("id", user.userId())
        .claim("name", user.name())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60))
        .signWith(getSecretKey())
        .compact();
  }

  public JwtUserPrincipal verifyToken(String token) {
    Claims claims =
        Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token).getPayload();

    Long id = claims.get("id", Long.class);
    String name = claims.get("name", String.class);
    String username = claims.getSubject();

    return new JwtUserPrincipal(id, name, username, null, new ArrayList<>());
  }

  public long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
    }

    JwtUserPrincipal principal = (JwtUserPrincipal) authentication.getPrincipal();
    if (principal == null || principal.userId() == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT token does not contain a user id");
    }
    return principal.userId();
  }
}
