package com.auth.Auth.Service.Mapper;

import com.auth.Auth.Service.DTO.Auth.*;
import com.auth.Auth.Service.DTO.User.UserResponse;
import com.auth.Auth.Service.Entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(SignupRequest dto);
    UserInfoDTO toUserInfoDTO(User user);
    UserResponse toUserResponseDTO(User user);
}