package com.chatbiti.userservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Token {
    @Id
    @GeneratedValue
    private Long id;

    private String token;
    private LocalDateTime creationDate;
    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @ManyToOne
    @JoinColumn(name = "\"user_id\"", nullable = false)
    private User user;

    public Token(String token, LocalDateTime expirationDate, TokenType tokenType, User user) {
        this.token = token;
        this.creationDate = LocalDateTime.now();
        this.expirationDate = expirationDate;
        this.tokenType = tokenType;
        this.user = user;
    }

    public Token() {
    }
}
