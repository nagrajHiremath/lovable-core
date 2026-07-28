package com.lovable.services.workspace_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "preview")
public class Preview {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  Long id;

  @OneToOne
  @JoinColumn(name = "project_id")
  Project project;

  String namespace;
  String podName;
  String podUrl;
  Instant startedAt;
  Instant terminatedAt;
}
