package com.raslan.taskmanager.repository;

import com.raslan.taskmanager.enums.TokenType;
import com.raslan.taskmanager.model.Token;
import com.raslan.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);
    void deleteByUserAndTokenType(User user, TokenType tokenType);
}
