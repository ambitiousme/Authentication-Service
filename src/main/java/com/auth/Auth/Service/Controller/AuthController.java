package com.auth.Auth.Service.Controller;

import com.auth.Auth.Service.DTO.Auth.*;
import com.auth.Auth.Service.Service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request)
    {
        SignupResponse response = authService.userSignup(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody AuthRequest request)
    {
        TokenResponse response = authService.userSignin(request);
        return  ResponseEntity.ok(response);
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(@RequestBody ForgetPasswordRequest request)
    {
        ForgetPasswordResponse serviceResponse = authService.forgetPassword(request);
        String response = "Hi "+serviceResponse.getName()+"..\nPassword reset link is sent to your email "+serviceResponse.getEmail()
                +" for username "+serviceResponse.getUsername()
                +".\nPlease wait for 15 minutes before trying again. \n\nThanks";
        return  ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request)
    {
        UpdatePasswordResponse serviceResponse = authService.resetPassword(request);
        String response = "Hi "+serviceResponse.getName()+".. Password has been successfully reset for username "+serviceResponse.getUsername();
        return  ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request)
    {
        UpdatePasswordResponse serviceResponse = authService.changePassword(request);
        String response = "Hi "+serviceResponse.getName()+".. Password is successfully changed for username "+serviceResponse.getUsername();
        return  ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshRequest request){

        TokenResponse response = authService.refreshtoken(request);
        return ResponseEntity.ok(response);
    }



}
