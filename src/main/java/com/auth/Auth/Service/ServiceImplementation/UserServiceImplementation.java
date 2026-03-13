package com.auth.Auth.Service.ServiceImplementation;

import com.auth.Auth.Service.Controller.UserController;
import com.auth.Auth.Service.DTO.Auth.UserInfoDTO;
import com.auth.Auth.Service.DTO.UpdateEmailRequest;
import com.auth.Auth.Service.DTO.UpdateUsernameRequest;
import com.auth.Auth.Service.DTO.UserRequest;
import com.auth.Auth.Service.DTO.UserResponse;
import com.auth.Auth.Service.Entity.User;
import com.auth.Auth.Service.Mapper.UserMapper;
import com.auth.Auth.Service.Repository.UserRepository;
import com.auth.Auth.Service.Security.CustomPrincipal;
import com.auth.Auth.Service.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class UserServiceImplementation implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImplementation.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthServiceImpl authService;

    public UserServiceImplementation(UserRepository userRepository, UserMapper userMapper, AuthServiceImpl authService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.authService = authService;
    }

    @Override
    public UserResponse getUser() {

        User user = userRepository.findById(getUserId()).orElseThrow(() -> new RuntimeException("Something went wrong."));
        return userMapper.toUserResponseDTO(user);
    }

    @Override
    public UserInfoDTO updateEmail(UpdateEmailRequest request) {


        User user = userRepository.findById(getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(request.getNewEmail());
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        // trigger verification email
        authService.sendEmailVerification(savedUser);

        return userMapper.toUserInfoDTO(savedUser);

    }

    @Override
    public UserInfoDTO updateUsername(UpdateUsernameRequest request) {


        User user = userRepository.findById(getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(request.getNewUsername());

        User updatedUser = userRepository.save(user);
        return userMapper.toUserInfoDTO(updatedUser);

    }

    @Override
    public UserResponse updateUser(UserRequest request) {

        User user = userRepository.findById(getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null)
            user.setName(request.getName());

        if (request.getContactNo() != null)
            user.setContactNo(request.getContactNo());

        if (request.getDob() != null)
            user.setDob(request.getDob());

        if (request.getAddress() != null)
            user.setAddress(request.getAddress());

        userRepository.save(user);

        return userMapper.toUserResponseDTO(user);
    }

    @Override
    public void deleteUser() {


        User user = userRepository.findById(getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
    }

    private String getUserId() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();

        return principal.getUserId();

    }
}
