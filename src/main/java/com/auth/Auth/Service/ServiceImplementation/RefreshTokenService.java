package com.auth.Auth.Service.ServiceImplementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private static final String PREFIX = "refresh:";
    private static final long EXPIRY = 7; // days


    private final StringRedisTemplate redisTemplate;

    public RefreshTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String create(String userId) {

        String token = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                PREFIX + token,
                userId,
                Duration.ofDays(EXPIRY)
        );

        return token;
    }

    public String validate(String token) {

        String userId = redisTemplate.opsForValue().get(PREFIX + token);

        if (userId == null) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        return userId;
    }

    public String rotate(String oldToken) {

        String userId = validate(oldToken);

        // delete old token
        redisTemplate.delete(PREFIX + oldToken);

        // create new token
        return create(userId);
    }
}
