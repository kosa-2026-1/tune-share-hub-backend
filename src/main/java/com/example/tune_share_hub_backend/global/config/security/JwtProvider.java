package com.example.tune_share_hub_backend.global.config.security;

import com.example.tune_share_hub_backend.dto.user.UserResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {
    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtProvider(
            @org.springframework.beans.factory.annotation.Value("${jwt.secret.key}") String secret,
            @org.springframework.beans.factory.annotation.Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}" ) long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String createJwt(String category, UserResponseDto userResponseDto) {
        long targetExpiration = category.equals("access") ? accessExpiration : refreshExpiration;

        return Jwts.builder()
                .subject(userResponseDto.getUserId().toString())
                .claim("category", category)
                .claim("email", userResponseDto.getEmail())
                .claim("role", userResponseDto.getRole().toString())
                .claim("nickname", userResponseDto.getNickname())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + targetExpiration))
                .signWith(key)
                .compact();
    }

    // access or refresh
    public String getCategory(String token) {
        return getClaims(token).get("category", String.class);
    }

    public Long getUserId(String token) {
        return getClaims(token).getSubject() != null ? Long.parseLong(getClaims(token).getSubject()) : null;
    }

    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public String getNickname(String token) {
        return getClaims(token).get("nickname", String.class);
    }

    // 파싱 로직 공통화
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
