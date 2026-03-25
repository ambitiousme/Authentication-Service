package com.auth.Auth.Service.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenType {
    EMAIL_VERIFICATION(
            "email-verify-user:",
            "email-verify-token:",
            60
    ),

    PASSWORD_RESET(
            "reset-password-user:",
            "reset-password-token:",
            15
    ),

    REFRESH_TOKEN(
            "refreshToken-user:",
            "refreshToken-token:",
            7 * 60
    );

    private final String userPrefix;
    private final String tokenPrefix;
    private final long expiryMinutes;
}