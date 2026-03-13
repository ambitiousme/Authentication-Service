package com.auth.Auth.Service.Security.Utility;

import com.auth.Auth.Service.Security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final long EXPIRATION_TIME = 10 * 60 * 60 * 1000;


    @Value("${security.jwt.secret-key}")
    private String secretKey;

    private String issuer = "auth-service";

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    private Key getSigningKey() {

        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public List<String> extractRoles(String token) {
        return extractClaim(token, claim -> claim.get("roles", List.class));
    }

    public String generateToken(CustomUserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();
        // claims.put("userId", userDetails.getUserId());
        // claims.put("roles", userDetails.getRoles());

        return generateToken(claims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, CustomUserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(Map<String, Object> extraClaims, CustomUserDetails userDetails, long expiration) {
        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUserId())
                .setIssuer(issuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    public String generateToken(String username) {
        return Jwts.builder().setSubject(username).setIssuedAt(new Date())
                .setIssuer(issuer)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    // This method will be used in case if jwt claim don't have roles and userid(optional) and we are loading userdetails from DB then we are going to verify token username and DB user username.
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean validateToken(String token) {
        String username = extractUsername(token);
        return !isTokenExpired(token);
    }

    // Validate token more validation to be added here
    public boolean validateToken(Claims claims) {

       boolean isExpired = claims.getExpiration().before(new Date());
       boolean correctIssuer = issuer.equals(claims.getIssuer());
        return !isExpired && correctIssuer;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
