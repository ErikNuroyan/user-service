package com.chatbiti.userservice;

import com.chatbiti.userservice.model.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    private TokenRepository tokenRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    private AuthenticationManager authenticationManager;

    private JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository,
                           TokenRepository tokenRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }


    @Override
    public UserRegisterResponseDto register(String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            return new UserRegisterResponseDto("failure", Optional.of("Email is already used!" ));
        }

        String roleName = "USER";
        Optional<Role> roleSearchResult = roleRepository.findByName(roleName);
        if (roleSearchResult.isEmpty()) {
            System.out.println("Error: Couldn't find the role: " + roleName);
            return new UserRegisterResponseDto("failure", Optional.of("Error during registration!"));
        }

        User user = new User(email,passwordEncoder.encode(password), firstName, lastName, false, List.of(roleSearchResult.get()));
        userRepository.save(user);

        return new UserRegisterResponseDto("success", null);
    }

    @Override
    public UserLoginResponseDto login(String email, String password) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        }
        catch (AuthenticationException exception) {
            return new UserLoginResponseDto("failure", Optional.of("Wrong email or password!"), null, null);
        }

        var claims = new HashMap<String, Object>();
        User user = ((User)authentication.getPrincipal());
        String token = jwtService.generateToken(user, claims);
        tokenRepository.save(new Token(
                token,
                convertToLocalDateTime(jwtService.extractExpiration(token)),
                TokenType.BEARER,
                user
        ));
        UserInfo userInfo = new UserInfo(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getEmailVerified()
        );

        return new UserLoginResponseDto("success", null, Optional.of(userInfo), Optional.of(token));
    }

    @Override
    public UserAuthResponseDto authenticate() {
        // If the flow reaches here through auth filters, it means authentication succeeded
        return new UserAuthResponseDto("success", Optional.empty());
    }

    @Override
    public UserSubscribeResponseDto subscribe(String token) {
        String email = jwtService.extractEmail(token);
        Optional<User> userSearchResult = userRepository.findByEmail(email);
        if (userSearchResult.isEmpty()) {
            return new UserSubscribeResponseDto("failure", Optional.of("Invalid token!"), Optional.empty());
        }

        String roleName = "PREMIUM_USER";
        User user = userSearchResult.get();
        if (user.getAuthorities().contains(new SimpleGrantedAuthority(roleName))) {
            return new UserSubscribeResponseDto("failure", Optional.of("Already a premium subscriber!"), Optional.empty());
        }

        Optional<Role> roleSearchResult = roleRepository.findByName(roleName);
        if (roleSearchResult.isEmpty()) {
            System.out.println("Error: Couldn't find the role: " + roleName);
            return new UserSubscribeResponseDto("failure", Optional.of("Error during subscription!"), Optional.empty());
        }

        user.addRole(roleSearchResult.get());

        Optional<Token> storedToken = tokenRepository.findByToken(token);
        if (storedToken.isEmpty()) {
            return new UserSubscribeResponseDto("failure", Optional.of("Invalid token!"), Optional.empty());
        }
        tokenRepository.delete(storedToken.get());

        String newToken = jwtService.generateToken(user, new HashMap<String, Object>());
        tokenRepository.save(new Token(
                token,
                convertToLocalDateTime(jwtService.extractExpiration(token)),
                TokenType.BEARER,
                user
        ));

        userRepository.save(user);

        return new UserSubscribeResponseDto("success", Optional.empty(), Optional.of(newToken));
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
