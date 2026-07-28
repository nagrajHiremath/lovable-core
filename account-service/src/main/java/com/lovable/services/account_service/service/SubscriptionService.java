package com.lovable.services.account_service.service;

import com.lovable.services.account_service.dto.subscription.SubscriptionResponse;
import com.lovable.services.common_lib.enums.SubscriptionStatus;

import java.time.Instant;

public interface SubscriptionService {
  SubscriptionResponse getMySubscription();

  void activateSubscription(Long userId, Long planId, String stripeSubscriptionId, String stripeCustomerId);

  void updateSubscription(
      String gatewaySubscriptionId,
      SubscriptionStatus status,
      Instant periodStart,
      Instant periodEnd,
      Boolean cancelAtPeriodEnd,
      Long planId);

  void cancelSubscription(String id);

  void renewSubscriptionPeriod(String subId, Instant periodStart, Instant periodEnd);

  void markSubscriptionPastDue(String subscriptionId);

//  boolean canCreateProject();
}
