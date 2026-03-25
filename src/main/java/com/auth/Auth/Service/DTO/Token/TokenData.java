package com.auth.Auth.Service.DTO.Token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenData {
    private String userId;
    private Map<String, String> metadata;
}
