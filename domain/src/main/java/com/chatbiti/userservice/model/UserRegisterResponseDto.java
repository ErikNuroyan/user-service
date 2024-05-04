package com.chatbiti.userservice.model;

import java.util.Optional;

public record UserRegisterResponseDto(String status, Optional<String> errorMessage) {
}
