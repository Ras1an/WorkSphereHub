package com.raslan.taskmanager.repository;

import com.raslan.taskmanager.enums.MembershipStatus;
import com.raslan.taskmanager.model.Workspace;
import com.raslan.taskmanager.model.WorkspaceUser;
import com.raslan.taskmanager.enums.WorkspaceUserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, Long>, JpaSpecificationExecutor<WorkspaceUser> {

    public boolean existsByUserIdAndWorkspaceIdAndRoleAndStatus(long userId, long workspaceId, WorkspaceUserRole role ,MembershipStatus status);

    public boolean existsByUserIdAndWorkspaceIdAndStatus(long userId, long workspaceId ,MembershipStatus status);



    @Query("""
            select wu.workspace
            from WorkspaceUser wu
            where wu.user.id = :userId
            and wu.status = com.raslan.taskmanager.enums.MembershipStatus.ACCEPTED
            """)
    List<Workspace> findWorkspacesByUserId(long userId);

    @Query("""
            select wu.workspace
            from WorkspaceUser wu
            where wu.user.id = :userId
            and wu.status = com.raslan.taskmanager.enums.MembershipStatus.PENDING
            """)
    Page<Workspace> findMembershipRequestsByUserId(long userId, Pageable pageable);

    public Optional<WorkspaceUser> findByUserIdAndWorkspaceId(long userId, long workspaceId);

}
