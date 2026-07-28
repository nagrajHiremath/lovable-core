package com.lovable.services.account_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "plan")
public class Plan {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  Long id;

  String name;

  String stripePriceId;
  Integer maxProjects;
  Integer maxTokensPerDay;
  Integer maxPreviews;
  Boolean unlimitedAI;
  Boolean isActive;
}
