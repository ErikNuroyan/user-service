package com.chatbiti.userservice;

import com.chatbiti.userservice.model.*;

public interface UserController {
    public UserRegisterResponseDto register(UserRegisterRequest registerRequest);

    public UserLoginResponseDto login(UserLoginRequest loginRequest);

    public UserAuthResponseDto authenticate();
}
