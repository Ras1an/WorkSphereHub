package com.raslan.taskmanager.model;

import com.raslan.taskmanager.enums.Priority;
import com.raslan.taskmanager.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="tasks", indexes = {@Index(name="idx_workspace", columnList = "workspace_id"),
                                @Index(name="idx_assigned_to", columnList = "assigned_to"),
                                @Index(name="idx_status", columnList = "status")
}
)

@SQLRestriction("""
        deletedAt is null
        and workspace_id in (select w.id from workspaces w where w.deletedAt is null)
        """)
public class Task {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String description;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Priority priority;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private LocalDateTime deadline;
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="created_by", nullable=true)
    private User createdBy;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="assigned_to", nullable=true)
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="workspace_id", nullable=false)
    private Workspace workspace;

    @OneToMany(mappedBy="task",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files;

}
