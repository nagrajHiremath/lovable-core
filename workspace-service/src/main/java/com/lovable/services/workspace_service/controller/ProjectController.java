package com.lovable.services.workspace_service.controller;


import com.lovable.services.workspace_service.dto.project.*;
import com.lovable.services.workspace_service.service.DeploymentService;
import com.lovable.services.workspace_service.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

  private final ProjectService projectService;
  private final DeploymentService deploymentService;

  @GetMapping()
  public ResponseEntity<List<ProjectSummaryResponse>> getMyProject() {
    return ResponseEntity.ok(projectService.getUserProjects());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
    return ResponseEntity.ok(projectService.getProjectById(id));
  }

  @PostMapping
  public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest projectRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(projectService.createProject(projectRequest));
  }

  @PatchMapping("{id}")
  public ResponseEntity<ProjectResponse> updateProject(
      @PathVariable Long id, @RequestBody ProjectUpdateRequest projectUpdateRequest) {
    return ResponseEntity.ok(projectService.updateProject(id, projectUpdateRequest));
  }

  @DeleteMapping("{id}")
  public ResponseEntity<String> softDeleteProject(@PathVariable Long id) {
    projectService.softDeleteProject(id);
    return ResponseEntity.ok("Delete Success");
  }
  @PostMapping("/{id}/deploy")
  public ResponseEntity<DeployResponse> deployProject(@PathVariable Long id) {
    return ResponseEntity.ok(deploymentService.deploy(id));
  }
}
