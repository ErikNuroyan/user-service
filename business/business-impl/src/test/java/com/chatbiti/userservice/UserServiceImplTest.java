package com.chatbiti.userservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.chatbiti.userservice.model.*;

class UserServiceImplTest {
    private UserServiceImpl userService;
    private UserRepository userRepository;
    private TokenRepository tokenRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        tokenRepository = mock(TokenRepository.class);
        roleRepository = mock(RoleRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);

        userService = new UserServiceImpl(userRepository, tokenRepository, roleRepository,
                passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void testRegister_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        String firstName = "John";
        String lastName = "Doe";
        Role role = new Role("USER");
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        // Act
        UserRegisterResponseDto response = userService.register(email, password, firstName, lastName);

        // Assert
        assertEquals("success", response.status());
        assertTrue(response.errorMessage().isEmpty());
    }

    @Test
    void testRegister_Failure_EmailAlreadyExists() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        String firstName = "John";
        String lastName = "Doe";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act
        UserRegisterResponseDto response = userService.register(email, password, firstName, lastName);

        // Assert
        assertEquals("failure", response.status());
        assertEquals("Email is already used!", response.errorMessage().orElse(null));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        User user = new User(email, password, "John", "Doe", false, List.of(new Role("USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(user, new HashMap<>())).thenReturn("token");
        when(jwtService.extractExpiration("token")).thenReturn(new Date(System.currentTimeMillis() + 309000));

        // Act
        UserLoginResponseDto response = userService.login(email, password);

        // Assert
        assertEquals("success", response.status());
        assertTrue(response.errorMessage().isEmpty());
        assertNotNull(response.token().orElse(null));
        assertNotNull(response.userInfo().orElse(null));
    }

    @Test
    void testLogin_Failure_WrongCredentials() {
        // Arrange
        String email = "test@example.com";
        String password = "password";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act
        UserLoginResponseDto response = userService.login(email, password);

        // Assert
        assertEquals("failure", response.status());
        assertEquals("Wrong email or password!", response.errorMessage().orElse(null));
        assertNull(response.token().orElse(null));
        assertNull(response.userInfo().orElse(null));
    }

    @Test
    void testAuthenticate() {
        // Act
        UserAuthResponseDto response = userService.authenticate();

        // Assert
        assertEquals("success", response.status());
        assertFalse(response.errorMessage().isPresent());
    }

    @Test
    void testSubscribe_Success() {
        // Arrange
        String token = "valid_token";
        User user = new User("test@example.com", "password", "John", "Doe", false, new ArrayList<>(List.of(new Role("USER"))));
        when(jwtService.extractEmail(token)).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(roleRepository.findByName("PREMIUM_USER")).thenReturn(Optional.of(new Role("PREMIUM_USER")));
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(new Token(token, LocalDateTime.now(), TokenType.BEARER, user)));
        when(jwtService.generateToken(user, new HashMap<>())).thenReturn("new_token");
        when(jwtService.extractExpiration("valid_token")).thenReturn(new Date(System.currentTimeMillis() + 309000));

        // Act
        UserSubscribeResponseDto response = userService.subscribe(token);

        // Assert
        assertEquals("success", response.status());
        assertFalse(response.errorMessage().isPresent());
        assertTrue(response.newToken().isPresent());
    }

    @Test
    void testSubscribe_Failure_InvalidToken() {
        // Arrange
        String token = "invalid_token";
        when(jwtService.extractEmail(token)).thenReturn(null);

        // Act
        UserSubscribeResponseDto response = userService.subscribe(token);

        // Assert
        assertEquals("failure", response.status());
        assertTrue(response.errorMessage().isPresent());
        assertFalse(response.newToken().isPresent());
    }

    @Test
    void testSubscribe_Failure_AlreadyPremium() {
        // Arrange
        String token = "valid_token";
        User user = new User("test@example.com", "password", "John", "Doe", false, List.of(new Role("PREMIUM_USER")));
        when(jwtService.extractEmail(token)).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        UserSubscribeResponseDto response = userService.subscribe(token);

        // Assert
        assertEquals("failure", response.status());
        assertTrue(response.errorMessage().isPresent());
        assertFalse(response.newToken().isPresent());
    }
}
