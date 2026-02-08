package com.example.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SupabaseJwtService {

    private static final int MIN_KEY_BYTES = 32;

    private final SecretKey secretKey;

    public SupabaseJwtService(
            @Value("${supabase.jwt.secret:supabase-jwt-secret-key-minimum-32-bytes-long}") String secret
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_KEY_BYTES) {
            keyBytes = java.util.Arrays.copyOf(keyBytes, MIN_KEY_BYTES);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ================= TOKEN PARSING =================

    @SuppressWarnings("deprecation")
	public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserId(String token) {
        return parseToken(token).getSubject();
    }
}