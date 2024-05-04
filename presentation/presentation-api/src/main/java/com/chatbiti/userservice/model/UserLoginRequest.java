package com.chatbiti.userservice.model;

import jakarta.validation.constraints.NotNull;

public record UserLoginRequest(@NotNull
                                String email,
                               @NotNull
                                String password) {
}
