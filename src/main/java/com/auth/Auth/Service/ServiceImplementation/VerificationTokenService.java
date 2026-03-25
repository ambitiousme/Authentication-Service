package com.auth.Auth.Service.ServiceImplementation;

import com.auth.Auth.Service.DTO.Token.TokenData;
import com.auth.Auth.Service.Enum.TokenType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class VerificationTokenService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public VerificationTokenService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public String createToken(String userId, TokenType type) {
        return createTokenInternal(userId, type);
    }

    public TokenData validateToken(String token, TokenType type) {
        return validateTokenInternal(token, type);
    }


    private String createTokenInternal(String userId, TokenType type) {

        String userKey = type.getUserPrefix() + userId;
        String oldToken = redisTemplate.opsForValue().get(userKey);

        if (oldToken != null) {
            redisTemplate.delete(type.getTokenPrefix() + oldToken);
        }

        String newToken = UUID.randomUUID().toString();

        TokenData data = new TokenData(userId, null);

        try {
            String json = objectMapper.writeValueAsString(data);

            redisTemplate.opsForValue().set(
                    type.getTokenPrefix() + newToken,
                    json,
                    Duration.ofMinutes(type.getExpiryMinutes())
            );

            redisTemplate.opsForValue().set(
                    userKey,
                    newToken,
                    Duration.ofMinutes(type.getExpiryMinutes())
            );

        } catch (Exception e) {
            throw new RuntimeException("Error creating token");
        }

        return newToken;
    }

    private TokenData validateTokenInternal(String token, TokenType type) {

        String tokenKey = type.getTokenPrefix() + token;

        String json = redisTemplate.opsForValue().get(tokenKey);

        if (json == null) {
            throw new RuntimeException("Invalid or expired token");
        }

        try {
            TokenData data = objectMapper.readValue(json, TokenData.class);

            redisTemplate.delete(type.getUserPrefix() + data.getUserId());
            redisTemplate.delete(tokenKey);

            return data;

        } catch (Exception e) {
            throw new RuntimeException("Parsing error");
        }
    }
}
