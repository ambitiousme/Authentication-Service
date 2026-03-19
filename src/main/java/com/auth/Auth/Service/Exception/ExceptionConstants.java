package com.auth.Auth.Service.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ExceptionConstants {
    public static final String EmailUsernameNotFound = "Username or Email does not exist.";
    public static final String INVALID_TOKEN = "Token is invalid.";
    public static final String INVALID_TOKEN_SIGNATURE = "Token Signature is invalid.";
    public static final String BLANK_TOKEN = "Token is blank.";
    public static final String TOKEN_EXPIRED = "Token is expired";
    public static final String INVALID_PASSWORD = "Password is invalid.";
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String SOMETHING_WENT_WRONG = "Something went wrong";
    public static final String INVALID_JWT_TOKEN = "Invalid or missing JWT token";
    public static final String UNAUTHORIZED = "You do not have permission to access this resources";
    public static final String INVALID_CREDENTIALS = "Invalid Username or Password";
    public static final String INVALID_TOKEN_FORMAT = "Token format is invalid";
    public static final String UNSUPPORTED_TOKEN = "Unsupported Token";
    public static final String EMAIL_EXIST = "Email Id Already Exist. Please try different email id.";
    public static final String USERNAME_EXIST = "Username already exist. Please try different username.";
}
