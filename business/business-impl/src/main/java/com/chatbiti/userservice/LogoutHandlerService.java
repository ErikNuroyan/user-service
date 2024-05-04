package com.chatbiti.userservice;

import com.chatbiti.userservice.model.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class LogoutHandlerService implements LogoutHandler {
    private TokenRepository tokenRepository;

    private ObjectMapper mapper;

    public LogoutHandlerService(TokenRepository tokenRepository, ObjectMapper mapper) {
        this.tokenRepository = tokenRepository;
        this.mapper = mapper;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String authHeader = request.getHeader(AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            fillResponseBody(response, "failure", "You are not logged in!");
            return;
        }

        String token = authHeader.substring(7);
        Optional<Token> storedToken = tokenRepository.findByToken(token);
        if (storedToken.isPresent()) {
            tokenRepository.delete(storedToken.get());
            fillResponseBody(response, "success", null);
        }
        else {
            fillResponseBody(response, "failure", "Invalid or expired token!");
        }
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
