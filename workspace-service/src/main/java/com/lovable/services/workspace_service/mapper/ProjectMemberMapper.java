package com.lovable.services.workspace_service.mapper;

import com.lovable.services.workspace_service.dto.member.MemberResponse;
import com.lovable.services.workspace_service.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

  @Mapping(target = "userId", source = "id.userId")
  MemberResponse toMemberResponse(ProjectMember projectMember);

  @Mapping(target = "userId", source = "id.userId")
  List<MemberResponse> toMemberResponseList(List<ProjectMember> projectMemberList);
}
