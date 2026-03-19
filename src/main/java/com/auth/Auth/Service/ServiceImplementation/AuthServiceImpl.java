package com.auth.Auth.Service.ServiceImplementation;

import com.auth.Auth.Service.Constants.ApplicationConstant;
import com.auth.Auth.Service.DTO.Auth.*;
import com.auth.Auth.Service.Entity.User;
import com.auth.Auth.Service.Entity.VerificationToken;
import com.auth.Auth.Service.Exception.*;
import com.auth.Auth.Service.Kafka.Events.PasswordResetEvent;
import com.auth.Auth.Service.Kafka.KafkaProducer;
import com.auth.Auth.Service.Mapper.UserMapper;
import com.auth.Auth.Service.Repository.UserRepository;
import com.auth.Auth.Service.Repository.VerificationTokenRepository;
import com.auth.Auth.Service.Security.CustomUserDetails;
import com.auth.Auth.Service.Security.Utility.JwtUtil;
import com.auth.Auth.Service.Security.Utility.RefreshTokenUtility;
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

import java.time.LocalDateTime;
import java.util.*;


@Service
public class AuthServiceImpl implements AuthService {

    private static final long RESET_TOKEN_EXPIRY = 15 * 60;
    private static final long EMAIL_VERIFICATION_TOKEN_EXPIRY = 30 * 24 * 60 * 60;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenService verificationTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final VerificationTokenRepository verificationTokenRepository;
    private final KafkaProducer kafkaProducer;

    public AuthServiceImpl(JwtUtil jwtUtil, AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService, VerificationTokenService verificationTokenService, PasswordEncoder passwordEncoder, UserRepository userRepo, UserMapper userMapper, VerificationTokenRepository verificationTokenRepository, KafkaProducer kafkaProducer) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.verificationTokenService = verificationTokenService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepo;
        this.userMapper = userMapper;
        this.verificationTokenRepository = verificationTokenRepository;
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
        }
        catch (BadCredentialsException ex)
        {
            throw new InvalidCredentialsException(ExceptionConstants.INVALID_CREDENTIALS);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        final String token = jwtUtil.generateToken(userDetails);
        final String refreshToken = refreshTokenService.create(userDetails.getUserId());

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

        Optional<User> isUserExist = userRepository.findByEmail(request.getEmailOrUsername()).or(() -> userRepository.findByUsername(request.getEmailOrUsername()));


        String token = UUID.randomUUID().toString();

        User user = isUserExist.orElseThrow(() -> new UsernameOrEmailNotFoundException(ExceptionConstants.EmailUsernameNotFound));

        VerificationToken verificationToken = new VerificationToken(user, token, LocalDateTime.now().plusSeconds(RESET_TOKEN_EXPIRY), ApplicationConstant.RESET_TOKEN);

        VerificationToken savedVerificationToken = verificationTokenRepository.save(verificationToken);

        UserInfoDTO response = userMapper.toUserInfoDTO(savedVerificationToken.getUser());

        String resetLink = "https://myapp.com/reset-password?token=" + token;

        // Publish event to Kafka
        PasswordResetEvent event = PasswordResetEvent.builder().email(user.getEmail()).username(user.getUsername()).resetLink(resetLink).build();

        kafkaProducer.publishPasswordResetEvent(event);

        return response;
    }

    @Override
    public UserInfoDTO resetPassword(ResetPasswordRequest request) {

        VerificationToken verificationToken = verificationTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException(ExceptionConstants.INVALID_TOKEN));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new RuntimeException(ExceptionConstants.TOKEN_EXPIRED);

        User user = verificationToken.getUser();

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        User updatedUser = userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

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
        String userId = refreshTokenService.validate(request.getRefreshToken());

        User user =  userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(new CustomUserDetails(user, new ArrayList<>()));
        String newRefreshToken = refreshTokenService.rotate(request.getRefreshToken());

        return new TokenResponse(token, newRefreshToken);

    }

//    @Override
//    public EmailVerificationResponse verifyEmail(String token) {
//
//       Map<String, String> userInfo = verificationTokenService.validateEmailToken(token);
//
//
//        Optional<VerificationToken> optionalToken = verificationTokenRepository.findByToken(token);
//
//        if (optionalToken.isEmpty()) {
//            throw new RuntimeException("Invalid verification link");
//        }
//
//        VerificationToken verificationToken = optionalToken.get();
//
//        User user = verificationToken.getUser();
//
//        if (user.isEmailVerified()) {
//            return new EmailVerificationResponse(true, "Email already verified");
//        }
//
//        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
//            return new EmailVerificationResponse(false, "Verification link expired");
//        }
//
//        user.setEmailVerified(true);
//        userRepository.save(user);
//        verificationTokenRepository.delete(verificationToken);
//
//        return new EmailVerificationResponse(true, "Email verified successfully");
//    }


    @Override
    public UserInfoDTO resendEmailVerification() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userId = authentication.getName();

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(ExceptionConstants.USER_NOT_FOUND));

//        Optional<VerificationToken> optionalToken = verificationTokenRepository.findByUserUsernameAndType(user.getUsername(), ApplicationConstant.VERIFICATION_TOKEN);
//
//        optionalToken.ifPresent(verificationTokenRepository::delete);

        sendEmailVerification(user);

        return userMapper.toUserInfoDTO(user);

    }


    public void sendEmailVerification(User user) {

      //  VerificationToken verificationToken = new VerificationToken(user, UUID.randomUUID().toString(), LocalDateTime.now().plusSeconds(EMAIL_VERIFICATION_TOKEN_EXPIRY), ApplicationConstant.VERIFICATION_TOKEN);

        String token = verificationTokenService.createEmailToken(user.getUserId(), user.getEmail());


        //verificationTokenRepository.save(verificationToken);

        //Need to send verification token using kafka to specified email for one time and store its token into db for verifying next time.

    }


}
