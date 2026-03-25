package com.auth.Auth.Service.Service;

import com.auth.Auth.Service.DTO.Auth.*;
import com.auth.Auth.Service.DTO.Token.TokenResponse;

public interface AuthService {


    UserInfoDTO forgetPassword(ForgetPasswordRequest forgetPasswordRequest);

    UserInfoDTO resetPassword(ResetPasswordRequest request);

    UserInfoDTO changePassword(ChangePasswordRequest updatePasswordRequestDTO);

    TokenResponse userSignin(AuthRequest authRequest);

    UserInfoDTO userSignup(SignupRequest signupRequest);

    TokenResponse refreshtoken(RefreshRequest request);

    EmailVerificationResponse verifyEmail(String token);

    UserInfoDTO resendEmailVerification();
}
