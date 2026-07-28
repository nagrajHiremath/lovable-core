package com.lovable.services.intelligence_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "usage_log")
@Getter
@Setter
public class UsageLog {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  Long id;

  @Column(name = "user_id", nullable = false)
  Long userId;

  LocalDate date;
  String action;
  Integer tokensUsed;
  String metadata;
  Instant createdAt;
}
