package com.auth.Auth.Service.Kafka.Events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetEvent {

    private String email;
    private String username;
    private String resetLink;

}
