package com.auth.Auth.Service.Security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class CustomPrincipal {
    private String userId;
    private String username;
    private String email;
}
