package com.auth.Auth.Service.DTO.Auth;


import lombok.Data;

@Data
public class ForgetPasswordRequest {
    private String emailOrUsername;
}
