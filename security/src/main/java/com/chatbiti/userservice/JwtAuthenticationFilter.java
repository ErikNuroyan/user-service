package com.chatbiti.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private JwtService jwtService;

    private UserDetailsService userDetailsService;

    private ObjectMapper mapper;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, ObjectMapper mapper) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.mapper = mapper;
    }


    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getServletPath().contains("/v1/user/login") ||
            request.getServletPath().contains("/v1/user/register") ||
            request.getServletPath().contains("/h2-console")) { // TODO: Remove this later
            filterChain.doFilter(request, response);
            return;
        }


        String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            fillResponseBody(response, "failure", "Unauthorised!");
            return;
        }

        String token = authHeader.substring(7);

        String email;
        try {
            email = jwtService.extractEmail(token);
        }
        catch (Exception e) {
            fillResponseBody(response, "failure", "Invalid token!");
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(email);
            }
            catch (Exception e) {
                fillResponseBody(response, "failure", "Invalid token!");
                return;
            }

            if (jwtService.isValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtService.isTokenExpired(token)) {
                jwtService.deleteToken(token);
                fillResponseBody(response, "failure", "Token expired!");
                return;
            }
            if (jwtService.isValidUser(token, userDetails)) {
                fillResponseBody(response, "failure", "Invalid user!");
                return;
            }

            // Token not present
            fillResponseBody(response, "failure", "Invalid token!");
            return;
        }

        if (email == null) {
            fillResponseBody(response, "failure", "Invalid token!");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void fillResponseBody(HttpServletResponse response, String status, String errorMessage) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> responseMap = new LinkedHashMap<>();
        responseMap.put("status", status);
        responseMap.put("errorMessage", errorMessage);
        try {
            mapper.writeValue(response.getWriter(), responseMap);
        } catch (IOException e) {
            // In general shouldn't reach here
            System.out.println("Couldn't get the response writer!");
        }
    }
}
