package com.lovable.services.workspace_service.service.impl;

import com.lovable.services.common_lib.security.AuthUtil;
import com.lovable.services.workspace_service.client.AccountClient;
import com.lovable.services.workspace_service.dto.member.InviteMemberRequest;
import com.lovable.services.workspace_service.dto.member.MemberResponse;
import com.lovable.services.workspace_service.dto.member.UpdateMemberRoleRequest;
import com.lovable.services.workspace_service.entity.Project;
import com.lovable.services.workspace_service.entity.ProjectMember;
import com.lovable.services.workspace_service.entity.ProjectMemberId;
import com.lovable.services.workspace_service.mapper.ProjectMemberMapper;
import com.lovable.services.workspace_service.repository.ProjectMemberRepository;
import com.lovable.services.workspace_service.repository.ProjectRepository;
import com.lovable.services.workspace_service.service.ProjectMemberService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

  ProjectMemberRepository projectMemberRepository;
  ProjectMemberMapper projectMemberMapper;
  AccountClient accountClient;
  ProjectRepository projectRepository;
  AuthUtil authUtil;

  public List<MemberResponse> getAllMembers(Long projectId) {
    return projectMemberMapper.toMemberResponseList(
        projectMemberRepository.findByProjectId(projectId));
  }

  public MemberResponse inviteMember(Long projectId, InviteMemberRequest inviteMemberRequest) {

    Long userId = authUtil.getCurrentUserId();
    Project project = projectRepository.findById(projectId).orElseThrow();
    ProjectMemberId projectMemberId = new ProjectMemberId(projectId, userId);

    ProjectMember projectMember =
        ProjectMember.builder()
            .id(projectMemberId)
            .projectRole(inviteMemberRequest.projectRole())
            .invitedAt(Instant.now())
            .project(project)
            .build();

    return projectMemberMapper.toMemberResponse(projectMemberRepository.save(projectMember));
  }

  public MemberResponse updateMemberRole(
      Long projectId, Long userId, UpdateMemberRoleRequest updateMemberRoleRequest) {
    ProjectMember projectMember =
        projectMemberRepository.findById(new ProjectMemberId(projectId, userId)).orElseThrow();
    projectMember.setProjectRole(updateMemberRoleRequest.projectRole());
    return projectMemberMapper.toMemberResponse(projectMemberRepository.save(projectMember));
  }

  public void removeProjectMember(Long projectId, Long userId) {
    projectMemberRepository.deleteById(new ProjectMemberId(projectId, userId));
  }
}
