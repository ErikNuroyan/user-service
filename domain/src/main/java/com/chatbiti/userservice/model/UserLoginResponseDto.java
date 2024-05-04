package com.chatbiti.userservice.model;

import java.util.Optional;

// TODO: Add conversations list to this
public record UserLoginResponseDto(String status,
                                   Optional<String> errorMessage,
                                   Optional<UserInfo> userInfo,
                                   Optional<String> token) {
}
