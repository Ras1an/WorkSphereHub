package com.raslan.taskmanager.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="files",
        indexes = {@Index(name="index_task", columnList = "task_id"),
                    @Index(name="index_workspace", columnList = "workspace_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="task_id", nullable = true)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="workspace_id",  nullable = true)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",  nullable = true)
    private User uploadedBy;

    @CreationTimestamp
    private LocalDateTime uploadedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void validateOwnership(){
        if( ((task == null) && (workspace == null)) || ( (task != null) && (workspace != null)) ){
            throw new IllegalStateException("File must belong to either task or workspace, not both.");
        }
    }

}
