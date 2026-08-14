package com.lovable.services.workspace_service.mapper;

import com.lovable.services.workspace_service.dto.project.ProjectRequest;
import com.lovable.services.workspace_service.dto.project.ProjectResponse;
import com.lovable.services.workspace_service.dto.project.ProjectSummaryResponse;
import com.lovable.services.workspace_service.dto.project.ProjectUpdateRequest;
import com.lovable.services.workspace_service.entity.Project;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
  Project toProjectEntity(ProjectRequest projectRequest);

  @Mapping(target = "role", ignore = true)
  ProjectResponse toProjectResponse(Project project);

  List<ProjectSummaryResponse> toListProjectSummaryResponse(List<Project> projects);

  @Mapping(target = "role", ignore = true)
  ProjectResponse toProjectResponse(ProjectUpdateRequest projectUpdateRequest);

  // IGNORE so a partial update (e.g. renaming only) doesn't null out fields the
  // request didn't include, like isPublic.
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateProject(
          ProjectUpdateRequest projectUpdateRequest, @MappingTarget Project existingProject);

  @Mapping(target = "role", ignore = true)
  ProjectSummaryResponse toProjectSummaryResponse(Project project);
}
