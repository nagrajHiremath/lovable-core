package com.lovable.services.workspace_service.service.impl;

import com.lovable.services.common_lib.dto.UserProfileResponse;
import com.lovable.services.common_lib.exception.ResourceNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ProjectMemberServiceImpl implements ProjectMemberService {

  ProjectMemberRepository projectMemberRepository;
  ProjectMemberMapper projectMemberMapper;
  AccountClient accountClient;
  ProjectRepository projectRepository;

  public List<MemberResponse> getAllMembers(Long projectId) {
    List<MemberResponse> members =
        projectMemberMapper.toMemberResponseList(projectMemberRepository.findByProjectId(projectId));

    Map<Long, UserProfileResponse> profilesByUserId =
        fetchProfiles(members.stream().map(MemberResponse::userId).toList());

    return members.stream().map(m -> enrich(m, profilesByUserId.get(m.userId()))).toList();
  }

  public MemberResponse inviteMember(Long projectId, InviteMemberRequest inviteMemberRequest) {
    UserProfileResponse invitedUser =
        accountClient
            .getUserByEmail(inviteMemberRequest.userName())
            .orElseThrow(
                () -> new ResourceNotFoundException("User", inviteMemberRequest.userName()));

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project", projectId.toString()));

    ProjectMemberId projectMemberId = new ProjectMemberId(projectId, invitedUser.userId());

    ProjectMember projectMember =
        ProjectMember.builder()
            .id(projectMemberId)
            .projectRole(inviteMemberRequest.projectRole())
            .invitedAt(Instant.now())
            .project(project)
            .build();

    MemberResponse saved =
        projectMemberMapper.toMemberResponse(projectMemberRepository.save(projectMember));
    return enrich(saved, invitedUser);
  }

  public MemberResponse updateMemberRole(
      Long projectId, Long userId, UpdateMemberRoleRequest updateMemberRoleRequest) {
    ProjectMember projectMember =
        projectMemberRepository.findById(new ProjectMemberId(projectId, userId)).orElseThrow();
    projectMember.setProjectRole(updateMemberRoleRequest.projectRole());
    MemberResponse saved =
        projectMemberMapper.toMemberResponse(projectMemberRepository.save(projectMember));
    return enrich(saved, fetchProfiles(List.of(userId)).get(userId));
  }

  public void removeProjectMember(Long projectId, Long userId) {
    projectMemberRepository.deleteById(new ProjectMemberId(projectId, userId));
  }

  private Map<Long, UserProfileResponse> fetchProfiles(List<Long> userIds) {
    if (userIds.isEmpty()) return Map.of();
    try {
      return accountClient.getUsersByIds(userIds).stream()
          .collect(Collectors.toMap(UserProfileResponse::userId, Function.identity()));
    } catch (Exception e) {
      log.error("Failed to resolve user profiles for members {}", userIds, e);
      return Map.of();
    }
  }

  private MemberResponse enrich(MemberResponse member, UserProfileResponse profile) {
    if (profile == null) return member;
    return new MemberResponse(
        member.userId(), profile.username(), profile.name(), member.projectRole(), member.invitedAt());
  }
}
