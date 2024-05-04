package com.chatbiti.userservice.model;

import java.util.Optional;

public record UserAuthResponseDto(String status, Optional<String> errorMessage) {
}
