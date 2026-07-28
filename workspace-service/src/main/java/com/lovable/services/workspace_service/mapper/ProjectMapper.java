package com.lovable.services.workspace_service.mapper;

import com.lovable.services.workspace_service.dto.project.ProjectRequest;
import com.lovable.services.workspace_service.dto.project.ProjectResponse;
import com.lovable.services.workspace_service.dto.project.ProjectSummaryResponse;
import com.lovable.services.workspace_service.dto.project.ProjectUpdateRequest;
import com.lovable.services.workspace_service.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
  Project toProjectEntity(ProjectRequest projectRequest);

  ProjectResponse toProjectResponse(Project project);

  List<ProjectSummaryResponse> toListProjectSummaryResponse(List<Project> projects);

  ProjectResponse toProjectResponse(ProjectUpdateRequest projectUpdateRequest);

  void updateProject(
          ProjectUpdateRequest projectUpdateRequest, @MappingTarget Project existingProject);

    ProjectSummaryResponse toProjectSummaryResponse(Project project);
}
