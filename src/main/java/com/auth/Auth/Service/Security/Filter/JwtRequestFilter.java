package com.auth.Auth.Service.Security.Filter;

import com.auth.Auth.Service.Exception.ExceptionConstants;
import com.auth.Auth.Service.Exception.JwtAuthenticationException;
import com.auth.Auth.Service.Security.CustomPrincipal;
import com.auth.Auth.Service.Security.Service.CustomUserDetailsService;
import com.auth.Auth.Service.Security.Utility.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customerUserDetailsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestFilter.class);

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


        LOGGER.info("entering jwt filter");
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            final String jwt = authHeader.substring(7);

            final String userId = jwtUtil.extractUseId(jwt);

            List<String> roles = jwtUtil.extractRoles(jwt);
            CustomPrincipal customPrincipal = CustomPrincipal.builder().userId(userId).build();

            List<GrantedAuthority> authorities;

            if(roles != null && !roles.isEmpty())
            authorities = roles.stream().map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role)).toList();
            else
                authorities = new ArrayList<>();

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null && jwtUtil.validateToken(jwt)) {

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customPrincipal, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

            }

            filterChain.doFilter(request, response);

        }
        catch (ExpiredJwtException ex) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new JwtAuthenticationException(ExceptionConstants.TOKEN_EXPIRED));
        }
        catch (MalformedJwtException ex) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new JwtAuthenticationException(ExceptionConstants.INVALID_TOKEN_FORMAT));
        }
        catch (SignatureException ex) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new JwtAuthenticationException(ExceptionConstants.INVALID_TOKEN_SIGNATURE));
        }
        catch (UnsupportedJwtException ex) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new JwtAuthenticationException(ExceptionConstants.UNSUPPORTED_TOKEN));
        }
        catch (IllegalArgumentException ex) {
            handlerExceptionResolver.resolveException(request, response, null,
                    new JwtAuthenticationException(ExceptionConstants.BLANK_TOKEN));
        }
        catch (Exception exception) {
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }

    }
}
