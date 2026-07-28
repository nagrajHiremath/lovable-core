package com.lovable.services.workspace_service.service;

import com.lovable.services.workspace_service.dto.project.ProjectRequest;
import com.lovable.services.workspace_service.dto.project.ProjectResponse;
import com.lovable.services.workspace_service.dto.project.ProjectSummaryResponse;
import com.lovable.services.workspace_service.dto.project.ProjectUpdateRequest;

import java.util.List;

public interface ProjectService {
  List<ProjectSummaryResponse> getUserProjects();

  ProjectResponse getProjectById(Long id);

  ProjectResponse createProject(ProjectRequest projectRequest);

  ProjectResponse updateProject(Long id, ProjectUpdateRequest projectUpdateRequest);

  void softDeleteProject(Long id);
}
