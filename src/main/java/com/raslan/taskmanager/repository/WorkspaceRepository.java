package com.raslan.taskmanager.repository;

import com.raslan.taskmanager.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace,Long>, JpaSpecificationExecutor<Workspace> {
    public Optional<Workspace> findByIdAndOwnerId(Long id, Long ownerId);
    public Optional<Workspace> findByCode(String code);
    public boolean existsByCode(String code);
}
