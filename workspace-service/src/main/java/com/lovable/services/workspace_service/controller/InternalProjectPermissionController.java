package com.lovable.services.workspace_service.controller;

import com.lovable.services.common_lib.enums.ProjectPermission;
import com.lovable.services.workspace_service.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Service-to-service endpoint, not exposed through the API gateway.
 * Used by intelligence-service to authorize project-scoped actions (e.g. chat/codegen)
 * against the calling user's actual project role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/projects/{projectId}/permissions")
public class InternalProjectPermissionController {

  private final ProjectMemberRepository projectMemberRepository;

  @GetMapping("/check")
  public ResponseEntity<Boolean> checkPermission(
      @PathVariable Long projectId,
      @RequestParam Long userId,
      @RequestParam ProjectPermission permission) {
    boolean allowed =
        projectMemberRepository
            .findRoleByProjectIdAndUserId(projectId, userId)
            .map(role -> role.getPermissions().contains(permission))
            .orElse(false);

    return ResponseEntity.ok(allowed);
  }
}
