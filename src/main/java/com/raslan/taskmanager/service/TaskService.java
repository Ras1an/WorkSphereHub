package com.raslan.taskmanager.service;

import com.raslan.taskmanager.dto.Task.CreateTaskDto;
import com.raslan.taskmanager.dto.Task.ReturnTaskDto;
import com.raslan.taskmanager.dto.Task.TaskFilter;
import com.raslan.taskmanager.dto.Task.UpdateTaskDto;
import com.raslan.taskmanager.enums.*;
import com.raslan.taskmanager.exception.BadRequestException;
import com.raslan.taskmanager.exception.ResourceNotFoundException;
import com.raslan.taskmanager.model.Task;
import com.raslan.taskmanager.model.User;
import com.raslan.taskmanager.model.Workspace;
import com.raslan.taskmanager.repository.TaskRepository;
import com.raslan.taskmanager.repository.UserRepository;
import com.raslan.taskmanager.repository.WorkspaceRepository;
import com.raslan.taskmanager.repository.WorkspaceUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;
    private final WorkspaceRepository workspaceRepo;
    private final WorkspaceUserRepository workspaceUserRepo;

    public TaskService(TaskRepository taskRepo,  UserRepository userRepo,   WorkspaceRepository workspaceRepo,   WorkspaceUserRepository workspaceUserRepo) {

        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
        this.workspaceRepo = workspaceRepo;
        this.workspaceUserRepo = workspaceUserRepo;
    }

    public ReturnTaskDto getById(Long taskId, Long userId) {
        Task task = taskRepo.findByIdAndAssignedToIdOrCreatedById(taskId, userId).orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        return toReturnTaskDto(task);
    }
    @Transactional
    public ReturnTaskDto createTask(Long userId, CreateTaskDto dto){
        User creator = getUser(userId);
        Workspace workspace = getWorkspace(dto.getWorkspaceId());
        User assignee = resolveAssignee(creator, dto.getAssignedToId());

        validateTaskCreationPermission(creator, assignee, workspace);

        Task task = buildTask(dto, creator, assignee, workspace);
        taskRepo.save(task);

        return toReturnTaskDto(task);
    }
    @Transactional
    public ReturnTaskDto updateTask(Long taskId, Long userId, UpdateTaskDto taskDto) {
        Task task = taskRepo.findTasksByIdAndCreatedById(taskId, userId).orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if(taskDto.getTitle() != null) task.setTitle(taskDto.getTitle());
        if(taskDto.getDescription() != null) task.setDescription(taskDto.getDescription());
        if(taskDto.getStatus() != null) task.setStatus(taskDto.getStatus());
        if(taskDto.getPriority() != null) task.setPriority(taskDto.getPriority());
        if(taskDto.getDeadline() != null) task.setDeadline(taskDto.getDeadline());

        return toReturnTaskDto(task);
    }
    @Transactional
    public ReturnTaskDto updateTaskStatus(Long taskId, Long userId, TaskStatus status) {
        Task task = taskRepo.findByIdAndAssignedToIdOrCreatedById(taskId, userId).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.setStatus(status);

        return toReturnTaskDto(task);
    }
    @Transactional
    public void deleteTask(Long id, Long useId){
        Task task = taskRepo.findTasksByIdAndCreatedById(id,  useId).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.setDeletedAt(LocalDateTime.now());
    }
    private ReturnTaskDto toReturnTaskDto(Task task){
        return ReturnTaskDto.builder().id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .deadline(task.getDeadline())
                .build();

    }
    public Page<ReturnTaskDto> getTasks(Long userId, TaskFilter taskFilter){
        LocalDateTime startDate = taskFilter.getStartDate();
        LocalDateTime endDate = taskFilter.getEndDate();
        if(startDate != null && endDate != null && endDate.isBefore(startDate))
             throw new BadRequestException("Start date can't be after end date");

        Specification<Task> spec = buildOwnershipSpec(userId).and(buildFilterSpec(taskFilter));
        String sortField = (taskFilter.getSortBy() != null)? taskFilter.getSortBy().getField()
                : "createdAt";

        Sort.Direction direction = Sort.Direction.DESC;
        if(taskFilter.getSortingDir() != null && taskFilter.getSortingDir() == SortingDirection.ASC)
            direction = Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(taskFilter.getPage(), taskFilter.getSize(), Sort.by(direction, sortField));

        return taskRepo.findAll(spec, pageable).map(this::toReturnTaskDto);
    }
    private Specification<Task> buildOwnershipSpec(Long userId) {
        return (root, query, cb) ->
                cb.or(
                        cb.equal(root.get("createdBy").get("id"), userId),
                        cb.equal(root.get("assignedTo").get("id"), userId)
                );
    }
    private Specification<Task> buildFilterSpec(TaskFilter filter){
        // title - assignedToId - createdById - workspaceId - status - priority - startDate - endDate
        Specification<Task> spec = (root, query, cb) -> cb.conjunction();

        if(filter.getTitle() != null && !filter.getTitle().isBlank()) spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + filter.getTitle().toLowerCase() + "%"));
        if(filter.getWorkspaceId() != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("workspace").get("id"), filter.getWorkspaceId()));
        if(filter.getStatus() != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        if(filter.getPriority() != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), filter.getPriority()));
        if(filter.getStartDate() != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));
        if(filter.getEndDate() != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));

        return spec;
    }

    private User getUser(Long id){
        return userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Workspace getWorkspace(Long id){
        return workspaceRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
    }
    private User resolveAssignee(User creator, Long assignedId){
        if(assignedId == null || assignedId.equals(creator.getId()))
            return creator;

        return getUser(assignedId);
    }

    private void validateTaskCreationPermission(User creator, User assignee, Workspace workspace){
        if(!workspace.getStatus().equals(WorkspaceStatus.ACTIVE))
            throw new AccessDeniedException("Workspace is not active");

        boolean creatorIsMember = workspaceUserRepo.existsByUserIdAndWorkspaceIdAndStatus(
                creator.getId(),
                workspace.getId(),
                MembershipStatus.ACCEPTED
        );

        if(!creatorIsMember)
            throw new AccessDeniedException("You are not a workspace member");

        boolean assigningToSelf = creator.getId().equals(assignee.getId());

        if(!assigningToSelf){
            boolean isManager = workspaceUserRepo.existsByUserIdAndWorkspaceIdAndRoleAndStatus(
                    creator.getId(),
                    workspace.getId(),
                    WorkspaceUserRole.MANAGER,
                    MembershipStatus.ACCEPTED

            );

            if(!isManager)
                throw new AccessDeniedException("Only managers can assign tasks");


            boolean assigneeIsMember = workspaceUserRepo.existsByUserIdAndWorkspaceIdAndStatus(
                    assignee.getId(),
                    workspace.getId(),
                    MembershipStatus.ACCEPTED
            );

            if(!assigneeIsMember)
                throw new BadRequestException("Assignee is not workspace member");
        }

    }

    private Task buildTask(CreateTaskDto dto, User creator, User assignee, Workspace workspace){

        return Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(TaskStatus.TODO)
                .priority(dto.getPriority())
                .createdBy(creator)
                .assignedTo(assignee)
                .workspace(workspace)
                .deadline(dto.getDeadline())
                .build();
    }
}
