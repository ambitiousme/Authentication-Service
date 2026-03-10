package com.auth.Auth.Service.DTO.Auth;

import lombok.Data;
import lombok.Getter;

@Data
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
}
