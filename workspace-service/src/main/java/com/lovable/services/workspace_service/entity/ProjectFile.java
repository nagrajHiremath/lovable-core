package com.lovable.services.workspace_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "project_file")
public class ProjectFile {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  Long id;

  @ManyToOne
  @JoinColumn(name = "projectId")
  Project project;

  String path;
  String minioObjectKey;

  //    User createdBy;
  //    User updatedBy;

  @CreationTimestamp Instant createdAt;

  @UpdateTimestamp Instant updatedAt;
}
