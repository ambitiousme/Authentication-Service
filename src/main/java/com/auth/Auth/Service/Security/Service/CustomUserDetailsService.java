package com.auth.Auth.Service.Security.Service;

import com.auth.Auth.Service.Entity.User;
import com.auth.Auth.Service.Exception.ExceptionConstants;
import com.auth.Auth.Service.Exception.InvalidCredentialsException;
import com.auth.Auth.Service.Exception.UsernameOrEmailNotFoundException;
import com.auth.Auth.Service.Repository.UserRepository;
import com.auth.Auth.Service.Security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionConstants.EmailUsernameNotFound));

        return new CustomUserDetails(user, new ArrayList<>());
    }
}
