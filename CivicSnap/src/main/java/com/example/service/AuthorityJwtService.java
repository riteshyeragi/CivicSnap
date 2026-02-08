package com.example.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthorityJwtService {

    private static final int MIN_KEY_BYTES = 32;

    private final SecretKey secretKey;

    public AuthorityJwtService(
            @Value("${authority.jwt.secret:civicsnap-authority-secret-key-min-32-bytes}") String secret,
            @Value("${authority.jwt.expiration-ms:86400000}") long expirationMs
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_KEY_BYTES) {
            keyBytes = java.util.Arrays.copyOf(keyBytes, MIN_KEY_BYTES);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ================= TOKEN GENERATION =================

    public String generateToken(String authorityId, Long communityId, String name) {
        return Jwts.builder()
                .setSubject(authorityId)
                .claim("communityId", communityId)
                .claim("name", name)
                .claim("role", "authority")
                .setIssuedAt(new Date())
                .setExpiration(new Date())
                .signWith(secretKey)
                .compact();
    }

    // ================= TOKEN PARSING =================

    @SuppressWarnings("deprecation")
	public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    // ================= HELPERS =================

    public String getAuthorityId(String token) {
        return parseToken(token).getSubject();
    }

    public Long getCommunityId(String token) {
        return parseToken(token).get("communityId", Long.class);
    }
}