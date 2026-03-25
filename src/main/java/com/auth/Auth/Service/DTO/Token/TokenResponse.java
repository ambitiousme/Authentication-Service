package com.auth.Auth.Service.DTO.Token;

import lombok.Data;

@Data
public class TokenResponse {

    private String token;
    private String refreshToken;

    public TokenResponse(String token, String refreshToken) {

        this.token = token;
        this.refreshToken = refreshToken;
    }
}