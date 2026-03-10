package com.auth.Auth.Service.Service;

import com.auth.Auth.Service.DTO.Auth.*;

public interface AuthService {


    ForgetPasswordResponse forgetPassword(ForgetPasswordRequest forgetPasswordRequest);

    UpdatePasswordResponse resetPassword(ResetPasswordRequest request);

    UpdatePasswordResponse changePassword(ChangePasswordRequest updatePasswordRequestDTO);

    TokenResponse userSignin(AuthRequest authRequest);

    SignupResponse userSignup(SignupRequest signupRequest);

    TokenResponse refreshtoken(RefreshRequest request);
}
