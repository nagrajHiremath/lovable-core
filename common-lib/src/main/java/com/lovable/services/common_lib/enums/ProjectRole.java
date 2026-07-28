package com.lovable.services.common_lib.enums;

import lombok.Getter;

import java.util.Set;

import static com.lovable.services.common_lib.enums.ProjectPermission.*;


@Getter
public enum ProjectRole {
  OWNER(VIEW, EDIT, DELETE, MANAGE_MEMBER),
  VIEWER(VIEW),
  DEVELOPER(DELETE, VIEW, EDIT);

  ProjectRole(ProjectPermission... permissions) {
    this.permissions = Set.of(permissions);
  }

  final Set<ProjectPermission> permissions;
}
