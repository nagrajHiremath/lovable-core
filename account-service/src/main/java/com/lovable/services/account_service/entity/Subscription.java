package com.lovable.services.account_service.entity;


import com.lovable.services.common_lib.enums.SubscriptionStatus;
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
@Table(name = "subscription")
@Getter
@Setter
public class Subscription {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  Long id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  User user;

  @OneToOne
  @JoinColumn(name = "plan_id")
  Plan plan;

  SubscriptionStatus status;

  String stripeCustomerId;
  String stripeSubscriptionId;
  Instant startDate;
  Instant endDate;
  Boolean cancelPeriodEnd = false;

  @CreationTimestamp
  Instant createdAt;

  @UpdateTimestamp
  Instant updatedAt;
}
