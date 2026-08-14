package com.lovable.services.workspace_service.controller;

import com.lovable.services.common_lib.exception.ResourceNotFoundException;
import com.lovable.services.workspace_service.dto.project.PublicProjectResponse;
import com.lovable.services.workspace_service.entity.Project;
import com.lovable.services.workspace_service.repository.ProjectRepository;
import com.lovable.services.workspace_service.service.DeploymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Anonymous, unauthenticated read access for projects the owner has explicitly
 * marked public. Reached through the API gateway's public-routes allowlist
 * (/api/v1/workspace/public/**), so there's no JWT/user context here at all.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/public/projects")
public class PublicProjectController {

  private final ProjectRepository projectRepository;
  private final DeploymentService deploymentService;

  @GetMapping("/{id}")
  public ResponseEntity<PublicProjectResponse> getPublicProject(@PathVariable Long id) {
    Project project =
        projectRepository
            .findById(id)
            .filter(p -> p.getDeletedAt() == null)
            .filter(p -> Boolean.TRUE.equals(p.getIsPublic()))
            .orElseThrow(() -> new ResourceNotFoundException("Project", id.toString()));

    return ResponseEntity.ok(
        new PublicProjectResponse(project.getId(), project.getName(), deploymentService.getPreviewUrl(id)));
  }
}
