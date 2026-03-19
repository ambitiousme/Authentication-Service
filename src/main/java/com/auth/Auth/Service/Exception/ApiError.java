package com.auth.Auth.Service.Exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Builder
@Data
public class ApiError {

    private String message;
    private int status;
    private LocalDateTime timestamp ;
    private String path;
    private String method;

}