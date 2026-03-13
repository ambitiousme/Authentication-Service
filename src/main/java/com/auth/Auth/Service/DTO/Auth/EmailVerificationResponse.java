package com.auth.Auth.Service.DTO.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class EmailVerificationResponse {
    private boolean success;
    private String message;
}