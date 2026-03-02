package com.raslan.taskmanager.repository;

import com.raslan.taskmanager.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    public List<File> findByTaskId(Long id);
    public List<File> findByWorkspaceId(Long id);
}
