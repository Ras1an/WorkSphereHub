package com.raslan.taskmanager.model;

import com.raslan.taskmanager.enums.WorkspaceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="workspaces", indexes = @Index(name="idx_code", columnList = "code"))
@SQLRestriction("deletedAt is null")
public class Workspace {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(nullable=false, unique=true)
    private String code;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private WorkspaceStatus status;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private LocalDateTime deadline;
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL,orphanRemoval = true,fetch = FetchType.LAZY)
    private List<Task> tasks;

    @OneToMany(mappedBy="workspace",cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.LAZY)
    private List<File> files;

    @OneToMany(mappedBy = "workspace",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkspaceUser> users;


    @ManyToOne
    @JoinColumn(name="owner_id", nullable=false)
    private User owner;
}
