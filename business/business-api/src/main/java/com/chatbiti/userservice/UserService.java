package com.chatbiti.userservice;

import com.chatbiti.userservice.model.UserAuthResponseDto;
import com.chatbiti.userservice.model.UserLoginResponseDto;
import com.chatbiti.userservice.model.UserRegisterResponseDto;
import com.chatbiti.userservice.model.UserSubscribeResponseDto;

public interface UserService {
    public UserRegisterResponseDto register(String email, String password, String firstName, String lastName);
    public UserLoginResponseDto login(String email, String password);
    public UserAuthResponseDto authenticate();
    public UserSubscribeResponseDto subscribe(String token);
}
