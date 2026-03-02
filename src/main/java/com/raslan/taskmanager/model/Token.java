package com.raslan.taskmanager.model;


import com.raslan.taskmanager.enums.TokenType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="tokens", indexes = @Index(name="idx_token", columnList = "token"))
@Getter
@Setter
@NoArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;


    public Token(
            String token,
            TokenType tokenType,
            LocalDateTime expiryDate,
            User user
    ) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiryDate = expiryDate;
        this.user = user;
    }

}
