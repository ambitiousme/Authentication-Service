package com.auth.Auth.Service.ServiceImplementation;

import java.util.ArrayList;
import com.auth.Auth.Service.DTO.Auth.*;
import com.auth.Auth.Service.DTO.Token.TokenData;
import com.auth.Auth.Service.DTO.Token.TokenResponse;
import com.auth.Auth.Service.Entity.User;
import com.auth.Auth.Service.Enum.TokenType;
import com.auth.Auth.Service.Exception.*;
import com.auth.Auth.Service.Kafka.Events.PasswordResetEvent;
import com.auth.Auth.Service.Kafka.KafkaProducer;
import com.auth.Auth.Service.Mapper.UserMapper;
import com.auth.Auth.Service.Repository.UserRepository;
import com.auth.Auth.Service.Security.CustomUserDetails;
import com.auth.Auth.Service.Security.Utility.JwtUtil;
import com.auth.Auth.Service.Service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final VerificationTokenService verificationTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaProducer kafkaProducer;

    public AuthServiceImpl(JwtUtil jwtUtil, AuthenticationManager authenticationManager, VerificationTokenService verificationTokenService,
                           PasswordEncoder passwordEncoder, UserRepository userRepo, UserMapper userMapper, KafkaProducer kafkaProducer) {

        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.verificationTokenService = verificationTokenService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepo;
        this.userMapper = userMapper;
        this.kafkaProducer = kafkaProducer;
    }

          /*
        AuthenticationManager
                 ↓
        DaoAuthenticationProvider
                 ↓
        UserDetailsService.loadUserByUsername()
                 ↓
        PasswordEncoder.matches(rawPassword, storedPassword)
        */

    @Override
    public TokenResponse userSignin(AuthRequest request) {

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException(ExceptionConstants.INVALID_CREDENTIALS);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        final String token = jwtUtil.generateToken(userDetails);
        final String refreshToken = verificationTokenService.createToken(userDetails.getUserId(), TokenType.REFRESH_TOKEN);

        return new TokenResponse(token, refreshToken);

    }

    @Override
    public UserInfoDTO userSignup(SignupRequest request) {

        boolean emailExist = userRepository.existsByEmail(request.getEmail());
        boolean usernameExist = userRepository.existsByUsername(request.getUsername());

        if (emailExist) throw new EmailAlreadyExistException(ExceptionConstants.EMAIL_EXIST);
        if (usernameExist) throw new UserAlreadyExistException(ExceptionConstants.USERNAME_EXIST);

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        sendEmailVerification(savedUser);

        return userMapper.toUserInfoDTO(user);

    }

    @Override
    public UserInfoDTO forgetPassword(ForgetPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmailOrUsername())
                .or(() -> userRepository.findByUsername(request.getEmailOrUsername()))
                .orElseThrow(() -> new UsernameOrEmailNotFoundException(ExceptionConstants.EmailUsernameNotFound));

        String token = verificationTokenService.createToken(user.getUserId(), TokenType.PASSWORD_RESET);

        UserInfoDTO response = userMapper.toUserInfoDTO(user);

        String resetLink = "https://localhost/reset-password?token=" + token;

        // Publish event to Kafka
        PasswordResetEvent event = PasswordResetEvent.builder().email(user.getEmail()).username(user.getUsername()).resetLink(resetLink).build();

        kafkaProducer.publishPasswordResetEvent(event);

        return response;
    }

    @Override
    public UserInfoDTO resetPassword(ResetPasswordRequest request) {

        TokenData data = verificationTokenService.validateToken(request.getToken(), TokenType.PASSWORD_RESET);

        User user = userRepository.findById(data.getUserId()).orElseThrow(() -> new UserNotFoundException(ExceptionConstants.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        User updatedUser = userRepository.save(user);

        return userMapper.toUserInfoDTO(updatedUser);


    }

    @Override
    public UserInfoDTO changePassword(ChangePasswordRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userId = authentication.getName();

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(ExceptionConstants.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new InvalidPasswordException(ExceptionConstants.INVALID_PASSWORD);

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        User updatedUser = userRepository.save(user);
        return userMapper.toUserInfoDTO(updatedUser);

    }


    @Override
    public TokenResponse refreshtoken(RefreshRequest request) {
        TokenData data =  verificationTokenService.validateToken(request.getRefreshToken(), TokenType.REFRESH_TOKEN);

        User user = userRepository.findById(data.getUserId()).orElseThrow(() -> new UserNotFoundException(ExceptionConstants.USER_NOT_FOUND));

        String token = jwtUtil.generateToken(new CustomUserDetails(user, new ArrayList<>()));
        String newRefreshToken = verificationTokenService.createToken(data.getUserId(), TokenType.REFRESH_TOKEN);

        return new TokenResponse(token, newRefreshToken);

    }

    @Override
    public EmailVerificationResponse verifyEmail(String token) {

        TokenData data = verificationTokenService.validateToken(token, TokenType.EMAIL_VERIFICATION);

        User user = userRepository.findById(data.getUserId()).orElseThrow(() -> new UserNotFoundException(ExceptionConstants.USER_NOT_FOUND));

        if (user.isEmailVerified())
            return new EmailVerificationResponse(true, "Email already verified");

        user.setEmailVerified(true);
        userRepository.save(user);

        return new EmailVerificationResponse(true, "Email verified successfully");
    }


    @Override
    public UserInfoDTO resendEmailVerification() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userId = authentication.getName();

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(ExceptionConstants.USER_NOT_FOUND));

        if (user.isEmailVerified())
            return new UserInfoDTO(user.getUsername(), user.getName(), user.getEmail(), "Email is already verified");

        sendEmailVerification(user);

        return userMapper.toUserInfoDTO(user);

    }

    public void sendEmailVerification(User user) {

        String token = verificationTokenService.createToken(user.getUserId(), TokenType.EMAIL_VERIFICATION);

        //Need to send verification token using kafka to specified email for one time and store its token into db for verifying next time.
    }

}
