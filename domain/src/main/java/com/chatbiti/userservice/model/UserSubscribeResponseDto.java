package com.chatbiti.userservice.model;

import java.util.Optional;

public record UserSubscribeResponseDto(String status, Optional<String> errorMessage, Optional<String> newToken) {
}
