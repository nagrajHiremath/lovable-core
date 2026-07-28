package com.lovable.services.workspace_service.controller;

import com.lovable.services.workspace_service.dto.member.InviteMemberRequest;
import com.lovable.services.workspace_service.dto.member.MemberResponse;
import com.lovable.services.workspace_service.dto.member.UpdateMemberRoleRequest;
import com.lovable.services.workspace_service.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/project/{projectId}/project-member")
public class ProjectMemberController {
  private final ProjectMemberService projectMemberService;

  @GetMapping
  public ResponseEntity<List<MemberResponse>> getAllMembers(@PathVariable Long projectId) {
    return ResponseEntity.ok(projectMemberService.getAllMembers(projectId));
  }

  @PostMapping("/invite")
  public ResponseEntity<MemberResponse> inviteMember(
      @PathVariable Long projectId, @RequestBody InviteMemberRequest inviteMemberRequest) {
    return ResponseEntity.ok(projectMemberService.inviteMember(projectId, inviteMemberRequest));
  }

  @PatchMapping
  public ResponseEntity<MemberResponse> updateMemberRole(
      @PathVariable Long projectId,
      @PathVariable Long userId,
      @RequestBody UpdateMemberRoleRequest updateMemberRoleRequest) {
    return ResponseEntity.ok(
        projectMemberService.updateMemberRole(projectId, userId, updateMemberRoleRequest));
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteMember(
      @PathVariable Long projectId, @PathVariable Long userId) {
    projectMemberService.removeProjectMember(projectId, userId);
    return ResponseEntity.noContent().build();
  }
}
