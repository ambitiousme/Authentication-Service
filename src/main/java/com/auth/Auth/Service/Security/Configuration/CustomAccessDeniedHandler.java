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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        LOGGER.warn("Exception occurred: {}", accessDeniedException.getMessage());
        ApiError error = ApiError.builder().path(request.getRequestURI()).message(ExceptionConstants.UNAUTHORIZED)
                .status(HttpStatus.FORBIDDEN.value())
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
