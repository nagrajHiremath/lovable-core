package com.lovable.services.workspace_service.repository;

import com.lovable.services.common_lib.enums.ProjectRole;
import com.lovable.services.workspace_service.entity.ProjectMember;
import com.lovable.services.workspace_service.entity.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
  List<ProjectMember> findByProjectId(Long projectId);

  ProjectRole findRoleByProjectIdAndUserId(long projectId, Long userId);

  int countProjectOwnedByUser(Long userId);
}
