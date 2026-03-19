package com.auth.Auth.Service.ServiceImplementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class VerificationTokenService {

    private static final String PREFIX = "email-verify:";
    private static final long EXPIRY_MINUTE = 60;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public VerificationTokenService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public String createEmailToken(String userId, String email) {

        String token = UUID.randomUUID().toString();

        Map<String, String> data = new HashMap<>();
        data.put("userId", userId);
        data.put("email", email);

        try {
            String json = objectMapper.writeValueAsString(data);

            redisTemplate.opsForValue().set(
                    PREFIX + token,
                    json,
                    Duration.ofMinutes(EXPIRY_MINUTE)
            );

        } catch (Exception e) {
            throw new RuntimeException("Error creating email token");
        }

        return token;
    }

    public String rotate(String oldToken) {

        String userId = validateEmailToken(oldToken);

        // delete old token
        redisTemplate.delete(PREFIX + oldToken);

        // create new token
        return create(userId);
    }

    public Map<String, String> validateEmailToken(String token) {

        String key = PREFIX + token;

        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            throw new RuntimeException("Invalid or expired token");
        }

        redisTemplate.delete(key);

        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Parsing error");
        }
    }
}
