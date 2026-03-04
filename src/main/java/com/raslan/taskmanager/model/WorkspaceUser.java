package com.raslan.taskmanager.model;

import com.raslan.taskmanager.enums.MembershipStatus;
import com.raslan.taskmanager.enums.WorkspaceUserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="workspace_user",
        uniqueConstraints = @UniqueConstraint(columnNames={"workspace_id", "user_id"}),
        indexes = @Index(name="idx_workspace_user", columnList = "workspace_id, user_id")
)
@SQLRestriction("deletedAt is null")
public class WorkspaceUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="workspace_id",  nullable=false)
    private Workspace workspace;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",  nullable=false)
    private User user;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private WorkspaceUserRole role;


    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private MembershipStatus status;

    @Column(nullable=false)
    @CreationTimestamp
    private LocalDateTime createdAt;


    private LocalDateTime joinedAt;

    private LocalDateTime deletedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
