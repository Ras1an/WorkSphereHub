package com.raslan.taskmanager.service;

import com.raslan.taskmanager.dto.Workspace.CreateWorkspaceDto;
import com.raslan.taskmanager.dto.Workspace.ReturnWorkspaceDto;
import com.raslan.taskmanager.dto.Workspace.UpdateWorkspaceDto;
import com.raslan.taskmanager.dto.Workspace.WorkspaceFilter;
import com.raslan.taskmanager.enums.*;
import com.raslan.taskmanager.exception.BadRequestException;
import com.raslan.taskmanager.exception.ConflictException;
import com.raslan.taskmanager.exception.ResourceNotFoundException;
import com.raslan.taskmanager.model.User;
import com.raslan.taskmanager.model.Workspace;
import com.raslan.taskmanager.model.WorkspaceUser;
import com.raslan.taskmanager.repository.UserRepository;
import com.raslan.taskmanager.repository.WorkspaceRepository;
import com.raslan.taskmanager.repository.WorkspaceUserRepository;
import com.raslan.taskmanager.util.CodeGenerator;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepo;
    private final WorkspaceUserRepository workspaceUserRepo;
    private final UserRepository userRepo;

    public WorkspaceService(WorkspaceRepository workspaceRepo,  UserRepository userRepo, WorkspaceUserRepository  workspaceUserRepo) {
        this.workspaceRepo = workspaceRepo;
        this.userRepo = userRepo;
        this.workspaceUserRepo = workspaceUserRepo;
    }

    public Page<ReturnWorkspaceDto> getAllWorkspaces(
            WorkspaceFilter filter,
            Long userId
    ) {

       Specification<Workspace> spec = buildWorkspaceSpecification(userId, filter);
        String sortField = SortBy.DEADLINE.equals(filter.getSortBy())
                ? "deadline"
                : "createdAt";

        Sort.Direction direction = SortingDirection.ASC.equals(filter.getSortingDir())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable =
                PageRequest.of(filter.getPage(), filter.getSize(), Sort.by(direction, sortField));

        Page<Workspace> workspaces = workspaceRepo.findAll(spec, pageable);

        return workspaces.map(this::toReturnWorkspaceDto);
    }

    public Page<ReturnWorkspaceDto> getAllMembershipRequests(Long userId, Pageable pageable) {
        User user = userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        if(!user.isVerified())
            throw new AccessDeniedException("Account not verified");

        return workspaceUserRepo.findMembershipRequestsByUserId(userId, pageable).map(this::toReturnWorkspaceDto);
    }

    @Transactional
    public ReturnWorkspaceDto createWorkspace(CreateWorkspaceDto workSpaceDto, Long userId){
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("the user who tried to create the workspace was not found"));
        if(!user.isVerified())
            throw new AccessDeniedException("Account not verified");

        String code;
        do{
            code = CodeGenerator.generateCode();
        }
        while(workspaceRepo.existsByCode(code));

        Workspace workspace = Workspace.builder()
                .code(code)
                .name(workSpaceDto.getName())
                .description(workSpaceDto.getDescription())
                .status(WorkspaceStatus.ACTIVE)
                .deadline(workSpaceDto.getDeadline())
                .owner(user)
                .build();


        workspaceRepo.save(workspace);


        // create WorkspaceUser object and save it
        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                                        .user(user)
                                        .workspace(workspace)
                                        .role(WorkspaceUserRole.MANAGER)
                                        .status(MembershipStatus.ACCEPTED)
                                        .joinedAt(LocalDateTime.now()).build();

        workspaceUserRepo.save(workspaceUser);
        return toReturnWorkspaceDto(workspace);
    }

    @Transactional
    public ReturnWorkspaceDto updateWorkspace(Long workspaceId, UpdateWorkspaceDto workSpaceDto, Long userId){
        WorkspaceUser workspaceUser = getActiveMembership(userId,  workspaceId);
        assertManager(workspaceUser);

        Workspace workspace = workspaceRepo.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("the workspace was not found"));

        if(workSpaceDto.getName() != null) workspace.setName(workSpaceDto.getName());
        if(workSpaceDto.getDescription() != null) workspace.setDescription(workSpaceDto.getDescription());
        if(workSpaceDto.getDeadline() != null) workspace.setDeadline(workSpaceDto.getDeadline());
        if(workSpaceDto.getWorkspaceStatus() != null){
            WorkspaceStatus oldStatus = workspace.getStatus();
            workspace.setStatus(workSpaceDto.getWorkspaceStatus());
            if(oldStatus != WorkspaceStatus.COMPLETED && workSpaceDto.getWorkspaceStatus() == WorkspaceStatus.COMPLETED)
                workspace.setFinishedAt(LocalDateTime.now());

        }

        return toReturnWorkspaceDto(workspace);
    }

    @Transactional
    public void deleteWorkspace(Long workspaceId, Long userId){
        Workspace workspace = workspaceRepo.findByIdAndOwnerId(workspaceId, userId).orElseThrow(() -> new ResourceNotFoundException("Something went wrong"));
        workspaceRepo.delete(workspace);
    }

    @Transactional
    public void inviteMemberToWorkspace(Long workspaceId, Long userId, String invitedUserEmail, WorkspaceUserRole role){

        Workspace workspace = workspaceRepo.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace was not found"));

        WorkspaceUser workspaceUser =  getActiveMembership(userId, workspaceId);
        assertManager(workspaceUser);

        User user =  userRepo.findByEmail(invitedUserEmail).orElseThrow(() -> new ResourceNotFoundException("user was not found"));
        if (!user.isVerified())
            throw new  AccessDeniedException("User email not verified");

        if (user.getId().equals(userId))
            throw new  AccessDeniedException("You cannot invite yourself");


       Optional<WorkspaceUser> optionalInvitedWorkspaceUser = workspaceUserRepo.findByUserIdAndWorkspaceId(user.getId(), workspaceId);
       if(optionalInvitedWorkspaceUser.isPresent()){
           WorkspaceUser invitedWorkspaceUser = optionalInvitedWorkspaceUser.get();
           if(invitedWorkspaceUser.getStatus() == MembershipStatus.ACCEPTED)
               throw new ConflictException("User is already in workspace");

           else if(invitedWorkspaceUser.getStatus() == MembershipStatus.PENDING)
               throw new ConflictException("User is already invited");

           else
               throw new ConflictException("Can not invite that user");
       }



       WorkspaceUser invitedWorkspaceUser = WorkspaceUser.builder()
                   .workspace(workspace)
                   .user(user)
                   .role(role)
                   .status(MembershipStatus.PENDING)
                   .build();

       workspaceUserRepo.save(invitedWorkspaceUser);

    }

    @Transactional
    public void updateMembershipStatus(Long workspaceId, Long userId, MembershipStatus status){
        WorkspaceUser workspaceUser = workspaceUserRepo.findByUserIdAndWorkspaceId(userId, workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace or user were not found"));
        if(workspaceUser.getStatus() != MembershipStatus.PENDING)
            throw new BadRequestException("Invitation is not in pending state");


        workspaceUser.setStatus(status);
    }

    @Transactional
    public void joinWorkspaceByCode(Long userId, String code){
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("user was not found"));
        if(!user.isVerified())
            throw new AccessDeniedException("User not verified");

        Workspace workspace = workspaceRepo.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Invalid code"));
        Optional<WorkspaceUser> existing = workspaceUserRepo.findByUserIdAndWorkspaceId(userId, workspace.getId());
        if(existing.isPresent()){
            if(existing.get().getStatus() == MembershipStatus.ACCEPTED)
                throw new  ConflictException("Already joined");
            if(existing.get().getStatus() == MembershipStatus.REMOVED)
                throw new ConflictException("You can not join this workspace");

            existing.get().setStatus(MembershipStatus.ACCEPTED);
            existing.get().setJoinedAt(LocalDateTime.now());
            existing.get().setRole(WorkspaceUserRole.MEMBER);

            return;
        }

        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceUserRole.MEMBER)
                .status(MembershipStatus.ACCEPTED)
                .joinedAt(LocalDateTime.now())
                .build();

        workspaceUserRepo.save(workspaceUser);
    }

    @Transactional
    public void leaveWorkspace(Long userId, Long workspaceId){
        WorkspaceUser membership = workspaceUserRepo.findByUserIdAndWorkspaceId(userId, workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace or user were not found"));
        if(membership.getStatus() != MembershipStatus.ACCEPTED)
            throw new AccessDeniedException("You are not even in that workspace");

        membership.setStatus(MembershipStatus.REMOVED);
    }

    @Transactional
    public void fireUser(Long managerId,Long firedId, Long workspaceId){
        if(managerId.equals(firedId))
            throw new BadRequestException("can not fire yourself");

        WorkspaceUser manager = getActiveMembership(managerId, workspaceId);
        assertManager(manager);
        WorkspaceUser membership = workspaceUserRepo.findByUserIdAndWorkspaceId(firedId, workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace or user were not found"));
        if(membership.getStatus() != MembershipStatus.ACCEPTED)
            throw new BadRequestException("User not active");

       Workspace workspace = workspaceRepo.findById(workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace was not found"));
       if(workspace.getOwner().getId().equals(firedId))
           throw new BadRequestException("You can not fire workspace owner");

        membership.setStatus(MembershipStatus.REMOVED);
    }

    public void uploadFile(){

    }
    private ReturnWorkspaceDto toReturnWorkspaceDto(Workspace workspace){
        ReturnWorkspaceDto returnWorkspaceDto = ReturnWorkspaceDto.builder()
                                                .id(workspace.getId())
                                                .name(workspace.getName())
                                                .code(workspace.getCode())
                                                .description(workspace.getDescription())
                                                .status(workspace.getStatus())
                                                .createdAt(workspace.getCreatedAt())
                                                .deadline(workspace.getDeadline())
                                                .build();


        return returnWorkspaceDto;
    }

    private WorkspaceUser getActiveMembership(Long userId, Long workspaceId){
        WorkspaceUser workspaceUser = workspaceUserRepo.findByUserIdAndWorkspaceId(userId, workspaceId).orElseThrow(() -> new ResourceNotFoundException("Workspace or user were not found"));

        if(!workspaceUser.getStatus().equals(MembershipStatus.ACCEPTED))
            throw new AccessDeniedException("You are not in that workspace");

        return workspaceUser;
    }

    private void assertManager(WorkspaceUser workspaceUser){
        if(workspaceUser.getRole() != WorkspaceUserRole.MANAGER)
            throw new AccessDeniedException("You are not manager in that workspace");
    }

    private Specification<Workspace> buildWorkspaceSpecification(Long userId, WorkspaceFilter filter){
        Specification<Workspace> spec = (root, query, cb) -> {
            query.distinct(true);
            Join<Workspace, WorkspaceUser> membership = root.join("users");

            Predicate byUser = cb.equal(membership.get("user").get("id"), userId);
            Predicate byStatus = cb.equal(membership.get("status"), MembershipStatus.ACCEPTED);

            return cb.and(byUser, byStatus);
        };

        if(filter.getName() != null && !filter.getName().isBlank())
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%"+ filter.getName().toLowerCase() + "%"));

        if(filter.getStatus() != null)
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), filter.getStatus()));

        if(filter.getStartDate() != null)
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));

        if(filter.getEndDate() != null)
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));


        return spec;
    }
}
