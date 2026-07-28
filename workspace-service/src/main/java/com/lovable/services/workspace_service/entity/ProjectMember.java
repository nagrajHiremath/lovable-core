package com.lovable.services.workspace_service.entity;


import com.lovable.services.common_lib.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "project_member")
public class ProjectMember {
  @EmbeddedId ProjectMemberId projectMemberId;

  @ManyToOne
  @MapsId("projectId")
  Project project;

  @Enumerated(EnumType.STRING)
  ProjectRole projectRole;

  Instant invitedAt;
  Instant acceptedAt;
}
