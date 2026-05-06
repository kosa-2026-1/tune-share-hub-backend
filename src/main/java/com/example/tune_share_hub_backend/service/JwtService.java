package com.example.tune_share_hub_backend.service;

import com.example.tune_share_hub_backend.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final byte[] secretKey;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${jwt.secret.key:tune-share-hub-secret-key-for-local-development}") String jwtSecretKey
    ) {
        this.objectMapper = objectMapper;
        this.secretKey = jwtSecretKey.getBytes(StandardCharsets.UTF_8);
    }

    public Long getUserId(String jwt) {
        JsonNode payload = parseAndValidate(jwt);
        // 로그인 담당 코드가 userId 클레임을 쓰면 userId를, subject를 쓰면 sub를 사용자 ID로 사용한다.
        JsonNode userIdNode = payload.has("userId") ? payload.get("userId") : payload.get("sub");

        if (userIdNode == null || userIdNode.isNull()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "토큰에서 사용자 ID를 찾을 수 없습니다.");
        }

        try {
            if (userIdNode.isNumber()) {
                return userIdNode.longValue();
            }
            return Long.valueOf(userIdNode.asText());
        } catch (NumberFormatException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "사용자 ID 형식이 올바르지 않습니다.");
        }
    }

    private JsonNode parseAndValidate(String jwt) {
        // JWT는 header.payload.signature 세 부분으로 구성된다.
        String[] tokenParts = jwt.split("\\.");
        if (tokenParts.length != 3) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
        }

        try {
            String headerJson = decodeToString(tokenParts[0]);
            JsonNode header = objectMapper.readTree(headerJson);
            validateAlgorithm(header);
            validateSignature(tokenParts);

            // 서명 검증 후 payload를 읽고 만료 시간을 확인한다.
            String payloadJson = decodeToString(tokenParts[1]);
            JsonNode payload = objectMapper.readTree(payloadJson);
            validateExpiration(payload);
            return payload;
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
        }
    }

    private String decodeToString(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private void validateAlgorithm(JsonNode header) {
        String algorithm = header.path("alg").asText();
        if (!"HS256".equals(algorithm)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "지원하지 않는 토큰 알고리즘입니다.");
        }
    }

    private void validateSignature(String[] tokenParts) throws Exception {
        String unsignedToken = tokenParts[0] + "." + tokenParts[1];
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secretKey, HMAC_SHA256));
        // header.payload를 같은 secret key로 다시 서명한 값과 JWT의 signature를 비교한다.
        String expectedSignature = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));

        if (!expectedSignature.equals(tokenParts[2])) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "토큰 서명이 올바르지 않습니다.");
        }
    }

    private void validateExpiration(JsonNode payload) {
        JsonNode expiration = payload.get("exp");
        if (expiration == null || expiration.isNull()) {
            return;
        }

        // exp는 JWT 표준상 초 단위 Unix time이다.
        long expirationSeconds = expiration.asLong();
        if (Instant.now().getEpochSecond() >= expirationSeconds) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다.");
        }
    }
}
