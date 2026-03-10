package com.auth.Auth.Service.DTO.Auth;

import lombok.Data;

@Data
public class ForgetPasswordResponse {

    private String username;
    private String email;
    private String name;
}
