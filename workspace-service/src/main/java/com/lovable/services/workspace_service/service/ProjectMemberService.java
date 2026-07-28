package com.lovable.services.workspace_service.service;

import com.lovable.services.workspace_service.dto.member.InviteMemberRequest;
import com.lovable.services.workspace_service.dto.member.MemberResponse;
import com.lovable.services.workspace_service.dto.member.UpdateMemberRoleRequest;

import java.util.List;

public interface ProjectMemberService {
  List<MemberResponse> getAllMembers(Long projectid);

  MemberResponse inviteMember(Long projectId, InviteMemberRequest inviteMemberRequest);

  MemberResponse updateMemberRole(
      Long projectId, Long userId, UpdateMemberRoleRequest updateMemberRoleRequest);

  void removeProjectMember(Long projectId, Long userId);
}
