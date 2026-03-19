package com.auth.Auth.Service.Security.Configuration;

import com.auth.Auth.Service.Exception.ApiError;
import com.auth.Auth.Service.Exception.ExceptionConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        LOGGER.warn("Exception occurred: {}", authException.getMessage());

        ApiError error = ApiError.builder().path(request.getRequestURI()).message(ExceptionConstants.INVALID_JWT_TOKEN)
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(LocalDateTime.now())
                .method(request.getMethod())
                .build();

        try {
            new ObjectMapper().writeValue(response.getOutputStream(), error);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred: {}", ex.getMessage(), ex);
        }

    }
}
