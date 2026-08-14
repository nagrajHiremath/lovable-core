package com.lovable.services.account_service.service.impl;


import com.lovable.services.account_service.dto.subscription.SubscriptionResponse;
import com.lovable.services.account_service.entity.Plan;
import com.lovable.services.account_service.entity.Subscription;
import com.lovable.services.account_service.entity.User;
import com.lovable.services.account_service.mapper.SubscriptionMapper;
import com.lovable.services.account_service.repository.PlanRepository;
import com.lovable.services.account_service.repository.SubscriptionRepository;
import com.lovable.services.account_service.repository.UserRepository;
import com.lovable.services.account_service.service.SubscriptionService;
import com.lovable.services.common_lib.enums.SubscriptionStatus;
import com.lovable.services.common_lib.exception.ResourceNotFoundException;
import com.lovable.services.common_lib.security.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

  private final SubscriptionRepository subscriptionrepository;
  private final AuthUtil authUtil;
  private final SubscriptionMapper subscriptionMapper;
  private final UserRepository userRepository;
  private final PlanRepository planRepository;
//  private final ProjectMemberRepository projectMemberRepository;
  Integer FREE_TIER_PROJECTS_ALLOWED = 1;

  public SubscriptionResponse getMySubscription() {

    Long userId = authUtil.getCurrentUserId();
    Subscription subscription =
        subscriptionrepository
            .findByUserIdAndStatusIn(
                userId,
                Set.of(
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.TRIALING,
                    SubscriptionStatus.PAST_DUE))
            .orElse(null);

    return subscriptionMapper.toSubscriptionResponse(subscription);
  }

  @Override
  public void activateSubscription(
      Long userId, Long planId, String stripeSubscriptionId, String stripeCustomerId) {

    boolean exist = subscriptionrepository.existsByStripeSubscriptionId(stripeSubscriptionId);

    if (exist) return;

    User user = getUser(userId);
    Plan plan = getPlan(planId);

    subscriptionrepository.save(
        Subscription.builder()
            .user(user)
            .plan(plan)
            .status(SubscriptionStatus.INCOMPLETE)
            .stripeCustomerId(stripeCustomerId)
            .stripeSubscriptionId(stripeSubscriptionId)
            .build());
  }

  @Override
  @Transactional
  public void updateSubscription(
      String gatewaySubscriptionId,
      SubscriptionStatus status,
      Instant periodStart,
      Instant periodEnd,
      Boolean cancelAtPeriodEnd,
      Long planId) {
    Subscription subscription = getSubscription(gatewaySubscriptionId);

    boolean hasSubscriptionUpdated = false;

    if (status != null && status != subscription.getStatus()) {
      subscription.setStatus(status);
      hasSubscriptionUpdated = true;
    }

    if (periodStart != null && !periodStart.equals(subscription.getStartDate())) {
      subscription.setStartDate(periodStart);
      hasSubscriptionUpdated = true;
    }

    if (periodEnd != null && !periodEnd.equals(subscription.getEndDate())) {
      subscription.setEndDate(periodEnd);
      hasSubscriptionUpdated = true;
    }

    if (cancelAtPeriodEnd != null && cancelAtPeriodEnd != subscription.getCancelPeriodEnd()) {
      subscription.setCancelPeriodEnd(cancelAtPeriodEnd);
      hasSubscriptionUpdated = true;
    }

    if (planId != null && !planId.equals(subscription.getPlan().getId())) {
      Plan newPlan = getPlan(planId);
      subscription.setPlan(newPlan);
      hasSubscriptionUpdated = true;
    }

    if (hasSubscriptionUpdated) {
      log.debug("Subscription has been updated: {}", gatewaySubscriptionId);
      subscriptionrepository.save(subscription);
    }
  }

  @Override
  public void cancelSubscription(String gatewaySubscriptionId) {
    Subscription subscription = getSubscription(gatewaySubscriptionId);
    subscription.setStatus(SubscriptionStatus.CANCELED);
    subscriptionrepository.save(subscription);
  }

  @Override
  public void renewSubscriptionPeriod(String subId, Instant periodStart, Instant periodEnd) {

    Subscription subscription = getSubscription(subId);
    subscription.setStartDate(periodStart);
    subscription.setEndDate(periodEnd);

    if (subscription.getStatus() == SubscriptionStatus.PAST_DUE
        || subscription.getStatus() == SubscriptionStatus.INCOMPLETE) {
      subscription.setStatus(SubscriptionStatus.ACTIVE);
    }
    subscriptionrepository.save(subscription);
  }

  @Override
  public void markSubscriptionPastDue(String subscriptionId) {

    Subscription subscription = getSubscription(subscriptionId);
    subscription.setStatus(SubscriptionStatus.PAST_DUE);
    subscriptionrepository.save(subscription);
  }

//  @Override
//  public boolean canCreateProject() {
//    Long userId = authUtil.getCurrentUserId();
//    SubscriptionResponse currentSubscription = getMySubscription();
//
//    int countOfOwnedProjects = projectMemberRepository.countProjectOwnedByUser(userId);
//
//    if (currentSubscription.plan() == null) {
//      return countOfOwnedProjects < FREE_TIER_PROJECTS_ALLOWED;
//    }
//
//    return countOfOwnedProjects < currentSubscription.plan().maxProjects();
//  }

  User getUser(Long userId) {
    return userRepository.findById(userId).orElseThrow();
  }

  Plan getPlan(Long planId) {
    return planRepository.findById(planId).orElseThrow();
  }

  Subscription getSubscription(String gatewaySubscriptionId) {
    return subscriptionrepository.findByStripeSubscriptionId(gatewaySubscriptionId).orElseThrow(() -> new ResourceNotFoundException("Subscription not found", gatewaySubscriptionId));
  }
}
