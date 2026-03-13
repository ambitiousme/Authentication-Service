package com.auth.Auth.Service.Service;


import com.auth.Auth.Service.DTO.Auth.UserInfoDTO;
import com.auth.Auth.Service.DTO.UpdateEmailRequest;
import com.auth.Auth.Service.DTO.UpdateUsernameRequest;
import com.auth.Auth.Service.DTO.UserRequest;
import com.auth.Auth.Service.DTO.UserResponse;

public interface UserService {
    UserResponse getUser();
    UserResponse updateUser(UserRequest request);
    UserInfoDTO updateEmail(UpdateEmailRequest request);
    UserInfoDTO updateUsername(UpdateUsernameRequest request);
    void deleteUser();
}
