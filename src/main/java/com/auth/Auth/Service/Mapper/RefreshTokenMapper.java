package com.auth.Auth.Service.Mapper;

import com.auth.Auth.Service.DTO.Auth.RefreshRequest;
import com.auth.Auth.Service.Entity.RefreshToken;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {
    RefreshRequest toDTO(RefreshToken rt);
}
