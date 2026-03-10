package com.auth.Auth.Service.Security.Utility;

import com.auth.Auth.Service.DTO.Auth.RefreshRequest;
import com.auth.Auth.Service.Entity.RefreshToken;
import com.auth.Auth.Service.Mapper.RefreshTokenMapper;
import com.auth.Auth.Service.Mapper.UserMapper;
import com.auth.Auth.Service.Repository.RefreshTokenRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class RefreshTokenUtility {

    private static final long REFRESH_EXPIRY_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenUtility(RefreshTokenRepository refreshTokenRepository, RefreshTokenMapper refreshTokenMapper){

        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken create (String username)
    {
        RefreshToken rt = new RefreshToken();
        rt.setUsername(username);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpriryDate(Instant.now().plusSeconds(REFRESH_EXPIRY_DAYS * 24 * 60 * 60));
        rt.setRevoked(false);
        return refreshTokenRepository.save(rt);

    }

    public RefreshToken validate(String token) {
        RefreshToken rt = refreshTokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("invalid refresh token"));
        if (rt.isRevoked())
            throw new RuntimeException("Token Revoked");
        if (rt.getExpriryDate().isBefore(Instant.now()))
            throw new RuntimeException("Token Expired");
        return rt;

    }

    public RefreshToken rotate(RefreshToken oldToken){
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        return create(oldToken.getUsername());
    }

    public void revokeAllUserToken(String username){
        refreshTokenRepository.findAll().stream().filter(rt -> rt.getUsername().equals("username"))
                .forEach(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });

    }
}
