package com.raslan.taskmanager.repository;

import com.raslan.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {


    @Query("""
            select t from task t
            where t.id = :id
            and (t.assignedTo.id = :userId or t.createdVy.id = :userId)
            """)
    public Optional<Task> findByIdAndAssignedToIdOrCreatedById(Long id, Long userId);
    public Optional<Task> findByIdAndAssignedToId(Long id, Long userId);

    public Optional<Task> findTasksByIdAndCreatedById(Long id, Long createdById);
}
