package com.raslan.taskmanager.model;

import com.raslan.taskmanager.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users", indexes = {@Index(name="idx_email", columnList = "email")})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private boolean verified;

    @OneToMany(mappedBy = "assignedTo",  fetch = FetchType.LAZY,  cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Task> tasks;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade =  CascadeType.ALL, orphanRemoval = true)
    private List<WorkspaceUser> workspaces;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Token> tokens;

    @OneToMany(mappedBy = "owner")
    private List<Workspace> ownedWorkspaces;

    public User(String name, String email, String password,  Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.verified = false;
    }

    public User(String name, String email, String password,  Role role,  boolean verified) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.verified = verified;
    }

}
