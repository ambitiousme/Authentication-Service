package com.auth.Auth.Service.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =  LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder().path(request.getRequestURI()).message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value()).timestamp(LocalDateTime.now())
                .method(request.getMethod())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {


        LOGGER.error("Exception occurred: {} ",ex.getMessage(), ex );

        ApiError error = ApiError.builder().path(request.getRequestURI()).message(ExceptionConstants.SOMETHING_WENT_WRONG)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .method(request.getMethod())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiError> handleInvalidPasswordException(InvalidPasswordException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder().path(request.getRequestURI()).message(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(LocalDateTime.now())
                .method(request.getMethod())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentialsException(InvalidCredentialsException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder().path(request.getRequestURI()).message(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(LocalDateTime.now())
                .method(request.getMethod())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExist(UserAlreadyExistException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder().path(request.getRequestURI()).message(ex.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .method(request.getMethod())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UsernameOrEmailNotFoundException.class)
    public ResponseEntity<ApiError> handleUsernameEmailNotFoundException(UsernameOrEmailNotFoundException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder().path(request.getRequestURI()).message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .method(request.getMethod())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }


}
