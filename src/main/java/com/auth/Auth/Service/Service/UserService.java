package com.auth.Auth.Service.Service;


import com.auth.Auth.Service.DTO.Auth.UserInfoDTO;
import com.auth.Auth.Service.DTO.User.UpdateEmailRequest;
import com.auth.Auth.Service.DTO.User.UpdateUsernameRequest;
import com.auth.Auth.Service.DTO.User.UserRequest;
import com.auth.Auth.Service.DTO.User.UserResponse;

public interface UserService {
    UserResponse getUser();
    UserResponse updateUser(UserRequest request);
    UserInfoDTO updateEmail(UpdateEmailRequest request);
    UserInfoDTO updateUsername(UpdateUsernameRequest request);
    void deleteUser();
}
