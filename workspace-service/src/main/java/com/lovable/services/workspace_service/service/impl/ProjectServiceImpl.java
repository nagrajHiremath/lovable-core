package com.lovable.services.workspace_service.service.impl;

import com.lovable.services.common_lib.enums.ProjectRole;
import com.lovable.services.common_lib.security.AuthUtil;
import com.lovable.services.workspace_service.dto.project.ProjectRequest;
import com.lovable.services.workspace_service.dto.project.ProjectResponse;
import com.lovable.services.workspace_service.dto.project.ProjectSummaryResponse;
import com.lovable.services.workspace_service.dto.project.ProjectUpdateRequest;
import com.lovable.services.workspace_service.entity.Project;
import com.lovable.services.workspace_service.entity.ProjectMember;
import com.lovable.services.workspace_service.entity.ProjectMemberId;
import com.lovable.services.workspace_service.mapper.ProjectMapper;
import com.lovable.services.workspace_service.repository.ProjectMemberRepository;
import com.lovable.services.workspace_service.repository.ProjectRepository;
import com.lovable.services.workspace_service.service.ProjectService;
import com.lovable.services.workspace_service.service.ProjectTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
  private final ProjectRepository projectRepository;
  private final ProjectMapper projectMapper;
  private final ProjectMemberRepository projectMemberRepository;
  private final AuthUtil authUtil;
  private final ProjectTemplateService projectTemplateService;

  public ProjectResponse createProject(ProjectRequest projectRequest) {

//    if (!subscriptionService.canCreateProject()) {
//      throw new RuntimeException("Cannot create project with current plan");
//    }

    Project project =
        Project.builder().name(projectRequest.name()).isPublic(projectRequest.isPublic()).build();
    project = projectRepository.save(project);

    ProjectMember projectMember =
        ProjectMember.builder()
            .project(project)
            .id(new ProjectMemberId(project.getId(), authUtil.getCurrentUserId()))
            .projectRole(ProjectRole.OWNER)
            .acceptedAt(Instant.now())
            .invitedAt(Instant.now())
            .build();

    projectMemberRepository.save(projectMember);

    projectTemplateService.initializeProjectFromTemplate(project.getId());

    ProjectResponse response = projectMapper.toProjectResponse(project);
    return new ProjectResponse(
        response.id(), response.name(), response.userId(), response.createdAt(),
        response.updatedAt(), response.deletedAt(), ProjectRole.OWNER);
  }

  public List<ProjectSummaryResponse> getUserProjects() {
    Long id = authUtil.getCurrentUserId();
    return projectRepository.findAllAccessibleByUser(id).stream()
        .map(projectWithRole -> {
          ProjectSummaryResponse summary =
              projectMapper.toProjectSummaryResponse(projectWithRole.getProject());
          return new ProjectSummaryResponse(
              summary.id(), summary.name(), summary.createdAt(), summary.updatedAt(),
              projectWithRole.getRole());
        })
        .toList();
  }

  public ProjectResponse getProjectById(Long id) {
    Project project = projectRepository.findById(id).orElseThrow();
    ProjectResponse response = projectMapper.toProjectResponse(project);
    ProjectRole role =
        projectMemberRepository
            .findRoleByProjectIdAndUserId(id, authUtil.getCurrentUserId())
            .orElse(null);
    return new ProjectResponse(
        response.id(), response.name(), response.userId(), response.createdAt(),
        response.updatedAt(), response.deletedAt(), role);
  }

  public ProjectResponse updateProject(Long id, ProjectUpdateRequest projectUpdateRequest) {
    Project existingProject = projectRepository.findById(id).orElseThrow();

    projectMapper.updateProject(projectUpdateRequest, existingProject);
    return projectMapper.toProjectResponse(projectRepository.save(existingProject));
  }

  public void softDeleteProject(Long id) {

    Project project = projectRepository.findById(id).orElseThrow();
    project.setDeletedAt(Instant.now());

    projectRepository.save(project);
  }
}
