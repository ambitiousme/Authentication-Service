package com.auth.Auth.Service.Security.Utility;

import com.auth.Auth.Service.Security.CustomUserDetails;
import com.auth.Auth.Service.Security.Filter.JwtRequestFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {


    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    private final String issuer = "auth-service";

    @Value("${security.jwt.expiration-time}")
    private long EXPIRATION_TIME;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private Map<String, Object> addClaims() {
        Map<String, Object> claims = new HashMap<>();
        // claims.put("userId", userDetails.getUserId());
        // claims.put("roles", userDetails.getRoles());
        return claims;
    }

    public String generateToken(CustomUserDetails userDetails) {
        return buildToken(addClaims(), userDetails, EXPIRATION_TIME);
    }

    private String buildToken(Map<String, Object> extraClaims, CustomUserDetails userDetails, long expiration) {

        LOGGER.info(String.valueOf(expiration));
        LOGGER.info("Expiration value: {}", (new Date(System.currentTimeMillis() + expiration)));

        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUserId())
                .setIssuer(issuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }




}
