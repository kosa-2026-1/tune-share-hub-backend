package com.example.tune_share_hub_backend.global.config.security;

import com.example.tune_share_hub_backend.dto.user.UserResponseDto;
import com.example.tune_share_hub_backend.global.exception.CustomException;
import com.example.tune_share_hub_backend.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
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
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String CLAIM_CATEGORY = "category";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_NICKNAME = "nickname";

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

    public String createAccessToken(UserResponseDto userResponseDto) {
        return createJwt(TOKEN_TYPE_ACCESS, userResponseDto, accessExpiration);
    }

    public String createRefreshToken(UserResponseDto userResponseDto) {
        return createJwt(TOKEN_TYPE_REFRESH, userResponseDto, refreshExpiration);
    }

    private String createJwt(String category, UserResponseDto userResponseDto, long expirationTime) {
        JwtBuilder builder = Jwts.builder()
                .subject(userResponseDto.getUserId().toString())
                .claim(CLAIM_CATEGORY, category)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key);

        if (TOKEN_TYPE_ACCESS.equals(category)) {
            builder.claim(CLAIM_EMAIL, userResponseDto.getEmail())
                    .claim(CLAIM_ROLE, userResponseDto.getRole().toString())
                    .claim(CLAIM_NICKNAME, userResponseDto.getNickname());
        }

        return builder.compact();
    }

    public String getTokenCategory(String token) {
        try {
            return getClaims(token).get(CLAIM_CATEGORY, String.class);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to extract token category: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

    public Long getUserId(String token) {
        validateToken(token);
        
        try {
            String subject = getClaims(token).getSubject();
            if (subject == null || subject.trim().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
            }
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            log.warn("Invalid user ID format in token: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

    public String getEmail(String token) {
        validateToken(token);
        
        String email = getClaims(token).get(CLAIM_EMAIL, String.class);
        if (email == null) {
            log.warn("Email claim not found in token");
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        return email;
    }

    public String getNickname(String token) {
        validateToken(token);
        
        String nickname = getClaims(token).get(CLAIM_NICKNAME, String.class);
        if (nickname == null) {
            log.warn("Nickname claim not found in token");
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        return nickname;
    }

    public String getRole(String token) {
        validateToken(token);

        String role = getClaims(token).get(CLAIM_ROLE, String.class);
        if (role == null) {
            log.warn("Role claim not found in token");
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        return role;
    }

    public boolean isAccessToken(String token) {
        try {
            return TOKEN_TYPE_ACCESS.equals(getTokenCategory(token));
        } catch (CustomException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return TOKEN_TYPE_REFRESH.equals(getTokenCategory(token));
        } catch (CustomException e) {
            return false;
        }
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

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return isExpired(claims);
        } catch (Exception e) {
            log.debug("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }

    private boolean isExpired(Claims claims) {
        return claims.getExpiration() != null && 
               claims.getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.debug("JWT parsing failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
    }
}
