package com.chatbiti.userservice;

import com.chatbiti.userservice.model.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TokenRepository extends CrudRepository<Token, Long> {
    Optional<Token> findByToken(String token);

    boolean existsByToken(String token);
}
