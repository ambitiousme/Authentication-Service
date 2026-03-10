package com.auth.Auth.Service.ServiceImplementation;

import com.auth.Auth.Service.DTO.Auth.*;
import com.auth.Auth.Service.Entity.RefreshToken;
import com.auth.Auth.Service.Entity.User;
import com.auth.Auth.Service.Exception.*;
import com.auth.Auth.Service.Kafka.Events.PasswordResetEvent;
import com.auth.Auth.Service.Kafka.KafkaProducer;
import com.auth.Auth.Service.Mapper.UserMapper;
import com.auth.Auth.Service.Repository.UserRepository;
import com.auth.Auth.Service.Security.Service.CustomUserDetailsService;
import com.auth.Auth.Service.Security.Utility.JwtUtil;
import com.auth.Auth.Service.Security.Utility.RefreshTokenUtility;
import com.auth.Auth.Service.Service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
public class AuthServiceImpl implements AuthService {


    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenUtility refreshTokenUtility;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaProducer kafkaProducer;

    public AuthServiceImpl(CustomUserDetailsService userDetailsService, JwtUtil jwtUtil, AuthenticationManager authenticationManager, RefreshTokenUtility refreshTokenUtility,
                           PasswordEncoder passwordEncoder, UserRepository userRepo, UserMapper userMapper, KafkaProducer kafkaProducer) {
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenUtility = refreshTokenUtility;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepo;
        this.userMapper = userMapper;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public ForgetPasswordResponse forgetPassword(ForgetPasswordRequest request) {


        Optional<User> isUserExist =
                userRepository.findByEmail(request.getEmailOrUsername())
                        .or(() -> userRepository.findByUsername(request.getEmailOrUsername()));


        User user = isUserExist.orElseThrow(() -> new UsernameOrEmailNotFoundException(ExceptionConstants.EmailUsernameNotFound));
        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(15));

        User updatedUser = userRepository.save(user);
        ForgetPasswordResponse response = userMapper.toForgetPasswordResponse(updatedUser);

        String resetLink = "https://myapp.com/reset-password?token=" + token;

        // Publish event to Kafka
        PasswordResetEvent event = PasswordResetEvent.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .resetLink(resetLink)
                .build();

        kafkaProducer.publishPasswordResetEvent(event);

        return response;
    }

    @Override
    public UpdatePasswordResponse resetPassword(ResetPasswordRequest request) {

        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException(ExceptionConstants.INVALID_TOKEN));

        if (user.getTokenExpiry().isBefore(LocalDateTime.now()))
            throw new RuntimeException(ExceptionConstants.TOKEN_EXPIRED);

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setResetToken(null);
        user.setTokenExpiry(null);

        User updatedUser = userRepository.save(user);

        return userMapper.toUpdatePasswordResponse(updatedUser);

    }

    @Override
    public UpdatePasswordResponse changePassword(ChangePasswordRequest request) {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String username = authentication.getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionConstants.USERNAME_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new InvalidPasswordException(ExceptionConstants.INVALID_PASSWORD);

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        User updatedUser = userRepository.save(user);
        return userMapper.toUpdatePasswordResponse(updatedUser);

    }

    @Override
    public TokenResponse userSignin(AuthRequest request) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String token = jwtUtil.generateToken(userDetails.getUsername());
        final String refreshToken = refreshTokenUtility.create(userDetails.getUsername()).getToken();

        return new TokenResponse(token, refreshToken);

    }

    @Override
    public SignupResponse userSignup(SignupRequest request) {

        boolean emailExist = userRepository.existsByEmail(request.getEmail());
        boolean usernameExist = userRepository.existsByUsername(request.getUsername());

        if (emailExist)
            throw new EmailAlreadyExistException("Email Id Already Exist. Please try other email id");
        if (usernameExist)
            throw new UserAlreadyExistException("Username already Exist. Please try other username");


        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        return userMapper.toSignupResponse(user);

    }

    @Override
    public TokenResponse refreshtoken(RefreshRequest request){
        RefreshToken refreshToken = refreshTokenUtility.validate(request.getRefreshToken());

        String token = jwtUtil.generateToken(refreshToken.getUsername());
        String newRefreshToken = refreshTokenUtility.rotate(refreshToken).getToken();

        return new TokenResponse(token, newRefreshToken);

    }


}
