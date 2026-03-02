package com.raslan.taskmanager.controller;

import com.raslan.taskmanager.dto.User.AddMemberDto;
import com.raslan.taskmanager.dto.User.FireUserDto;
import com.raslan.taskmanager.dto.Workspace.*;
import com.raslan.taskmanager.enums.MembershipStatus;
import com.raslan.taskmanager.security.UserPrincipal;
import com.raslan.taskmanager.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {
    private final WorkspaceService workspaceService;
    public WorkspaceController( WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public ResponseEntity<Page<ReturnWorkspaceDto>>  getUserWorkspaces(
            @RequestParam WorkspaceFilter filter,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return ResponseEntity.ok(workspaceService.getAllWorkspaces(filter, user.getId()));
    }

    @GetMapping("/requests")
    public ResponseEntity<Page<ReturnWorkspaceDto>>  getMembershipRequests(Pageable pageable, @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(workspaceService.getAllMembershipRequests(user.getId(), pageable));
    }

    @PostMapping
    public ResponseEntity<ReturnWorkspaceDto> createWorkspace(@Valid @RequestBody CreateWorkspaceDto workSpaceDto, @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(workspaceService.createWorkspace(workSpaceDto, user.getId()));
    }

    @PatchMapping("/{workspaceId}")
    public ResponseEntity<ReturnWorkspaceDto> updateWorkspace(@PathVariable Long workspaceId, @Valid @RequestBody UpdateWorkspaceDto workspaceDto, @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(workspaceService.updateWorkspace(workspaceId, workspaceDto, user.getId()));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<String> deleteWorkspace(@PathVariable Long workspaceId, @AuthenticationPrincipal UserPrincipal user) {
        workspaceService.deleteWorkspace(workspaceId, user.getId());
        return  ResponseEntity.ok("Workspace has been deleted");
    }

    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<String> inviteMember(@PathVariable Long workspaceId, @Valid @RequestBody AddMemberDto dto, @AuthenticationPrincipal UserPrincipal user) {
        workspaceService.inviteMemberToWorkspace(workspaceId, user.getId(), dto.getInvitedUserEmail(), dto.getRole());
        return ResponseEntity.ok("Member has been invited");
    }

    @PutMapping("/{workspaceId}/members/accept")
    public ResponseEntity<String> acceptInvitation(@PathVariable Long workspaceId, @AuthenticationPrincipal UserPrincipal user) {
        workspaceService.updateMembershipStatus(workspaceId, user.getId(), MembershipStatus.ACCEPTED);
        return ResponseEntity.ok("Membership has been accepted");
    }

    @PutMapping("/{workspaceId}/members/reject")
    public ResponseEntity<String> rejectInvitation(@PathVariable Long workspaceId, @AuthenticationPrincipal UserPrincipal user) {
        workspaceService.updateMembershipStatus(workspaceId, user.getId(), MembershipStatus.REJECTED);
        return ResponseEntity.ok("Membership has been rejected");
    }

    @PostMapping("/join")
    public ResponseEntity<String> joinWorkspaceByCode(@Valid @RequestBody JoinWorkspaceDto dto, @AuthenticationPrincipal UserPrincipal user) {
        workspaceService.joinWorkspaceByCode(user.getId(),  dto.getCode());
        return ResponseEntity.ok("Workspace has been joined");
    }

    @PatchMapping("{workspaceId}/members/fire")
    public ResponseEntity<String> fireWorkspaceUser(@PathVariable Long workspaceId, @Valid @RequestBody FireUserDto firedUserDto, @AuthenticationPrincipal UserPrincipal user) {
        workspaceService.fireUser(user.getId(), firedUserDto.getUserId(), workspaceId);
        return ResponseEntity.ok("User has been fired");
    }


    @PatchMapping("{workspaceId}/members/leave")
    public ResponseEntity<String> leaveWorkspace(@PathVariable Long workspaceId, @AuthenticationPrincipal UserPrincipal user) {
        workspaceService.leaveWorkspace(user.getId(), workspaceId);

        return  ResponseEntity.ok("Workspace has been leaved");
    }
}
