package com.chatbiti.userservice;

import com.chatbiti.userservice.model.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
