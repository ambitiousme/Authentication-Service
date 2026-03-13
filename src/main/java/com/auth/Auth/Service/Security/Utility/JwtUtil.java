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

    private long getExpirationTime() {
        return EXPIRATION_TIME;
    }

    private Map<String, Object> addClaims(){
        Map<String, Object> claims = new HashMap<>();
        // claims.put("userId", userDetails.getUserId());
        // claims.put("roles", userDetails.getRoles());
        return claims;
    }

    private String buildToken(Map<String, Object> extraClaims, CustomUserDetails userDetails, long expiration) {

        LOGGER.info(String.valueOf(expiration));
        LOGGER.info("Expiration value: {}", (new Date(System.currentTimeMillis() + expiration))) ;

        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUserId())
                .setIssuer(issuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }


    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        LOGGER.info("entirng jwtutil");
        try {
            final Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();

            LOGGER.info("entirng jwtutil"+ claims);
            return claimsResolver.apply(claims);
        }
        catch (RuntimeException ex)
        {
            ex.getMessage();
        }
 return  null;

    }

    public String generateToken(CustomUserDetails userDetails) {
        return buildToken(addClaims(), userDetails, EXPIRATION_TIME);
    }

    // Validate token more validation to be added here
    public boolean validateToken(String token) {

        boolean isExpired = extractExpiration(token).before(new Date());
        boolean correctIssuer = issuer.equals(extractIssuer(token));

        return !isExpired && correctIssuer;
    }

    public String extractUseId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public List extractRoles(String token) {
        return extractClaim(token, claim -> claim.get("roles", List.class));
    }

    public String extractIssuer(String token) {
        return extractClaim(token, Claims::getIssuer);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
