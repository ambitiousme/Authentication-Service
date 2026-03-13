package com.auth.Auth.Service.Security.Filter;

import com.auth.Auth.Service.Security.CustomPrincipal;
import com.auth.Auth.Service.Security.Service.CustomUserDetailsService;
import com.auth.Auth.Service.Security.Utility.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customerUserDetailsService;

    private final JwtUtil jwtUtil;

    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtRequestFilter(JwtUtil jwtUtil, CustomUserDetailsService customerUserDetailsService,
                            HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtUtil = jwtUtil;
        this.customerUserDetailsService = customerUserDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtUtil.extractUsername(jwt);
            final String userId = jwtUtil.extractUserId(jwt);

            List<String> roles = jwtUtil.extractRoles(jwt);

            CustomPrincipal customPrincipal = new CustomPrincipal(userId,username);

            List<GrantedAuthority> authorities = roles.stream().map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role)).toList();

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null && jwtUtil.validateToken(jwt)) {

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customPrincipal, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

            }

            filterChain.doFilter(request, response);

        } catch (Exception exception) {
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }

    }
}
