package com.teamtask.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        log.debug("[JWT Filter] {} {} - Authorization header present: {}", method, requestURI, authHeader != null);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("[JWT Filter] {} {} - No Bearer token found, skipping JWT authentication", method, requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        log.debug("[JWT Filter] {} {} - Bearer token found (length: {})", method, requestURI, jwt.length());

        try {
            userEmail = jwtUtil.extractUsername(jwt);
            log.info("[JWT Filter] {} {} - Extracted email from token: {}", method, requestURI, userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("[JWT Filter] {} {} - Loading user details for: {}", method, requestURI, userEmail);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.debug("[JWT Filter] {} {} - User found: {}, authorities: {}", method, requestURI, userEmail, userDetails.getAuthorities());

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("[JWT Filter] {} {} - Authentication successful for user: {}", method, requestURI, userEmail);
                } else {
                    log.warn("[JWT Filter] {} {} - Token validation failed for user: {}", method, requestURI, userEmail);
                }
            }
        } catch (Exception e) {
            log.error("[JWT Filter] {} {} - Cannot set user authentication: {} - {}", method, requestURI, e.getClass().getSimpleName(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
