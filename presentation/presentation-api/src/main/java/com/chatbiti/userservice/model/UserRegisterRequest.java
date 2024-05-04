package com.chatbiti.userservice.model;

import jakarta.validation.constraints.NotNull;

public record UserRegisterRequest(@NotNull
                                  String email,
                                  @NotNull
                                  String password,
                                  @NotNull
                                  String firstName,
                                  @NotNull
                                  String lastName) {
}
