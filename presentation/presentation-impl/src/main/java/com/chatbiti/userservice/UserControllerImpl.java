package com.chatbiti.userservice;

import com.chatbiti.userservice.model.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v1/user")
public class UserControllerImpl implements UserController {
    private final UserService userService;

    public UserControllerImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    @PostMapping("/register")
    public UserRegisterResponseDto register(@RequestBody @Valid UserRegisterRequest registerRequest) {
        return userService.register(registerRequest.email(),
                                              registerRequest.password(),
                                              registerRequest.firstName(),
                                              registerRequest.lastName());
    }

    @Override
    @PostMapping("/login")
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequest loginRequest) {
        return userService.login(loginRequest.email(), loginRequest.password());
    }

    @Override
    @PostMapping("/authenticate")
    public UserAuthResponseDto authenticate() {
        return userService.authenticate();
    }

    @Override
    @PostMapping("/subscribe")
    public UserSubscribeResponseDto subscribe(@RequestHeader("Authorization") String authorizationHeader) {
        // This shouldn't happen in general since Authentication filter will not let the request reach here
        // This is just a sanity check
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return new UserSubscribeResponseDto("failure", Optional.of("Unauthorised!"), Optional.empty());
        }

        String token = authorizationHeader.substring(7);
        return userService.subscribe(token);
    }
}
