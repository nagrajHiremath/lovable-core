package com.lovable.services.account_service.controller;

import com.lovable.services.account_service.entity.Plan;
import com.lovable.services.account_service.entity.Subscription;
import com.lovable.services.account_service.repository.PlanRepository;
import com.lovable.services.account_service.repository.SubscriptionRepository;
import com.lovable.services.common_lib.dto.PlanResponse;
import com.lovable.services.common_lib.enums.SubscriptionStatus;
import com.lovable.services.common_lib.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Service-to-service endpoints, not exposed through the API gateway.
 * Used by intelligence-service to resolve a user's current plan (for AI usage limits)
 * without going through public auth.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/billing")
public class InternalBillingController {

  private final SubscriptionRepository subscriptionRepository;
  private final PlanRepository planRepository;

  @GetMapping("/current-plan")
  public ResponseEntity<PlanResponse> getCurrentPlan(@RequestParam("userId") Long userId) {
    Plan plan =
        subscriptionRepository
            .findByUserIdAndStatusIn(
                userId,
                Set.of(
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.TRIALING,
                    SubscriptionStatus.PAST_DUE))
            .map(Subscription::getPlan)
            // Free users never get a subscription row at all, so fall back to the
            // seeded "Free" plan's real limits instead of failing.
            .orElseGet(
                () ->
                    planRepository
                        .findByName("Free")
                        .orElseThrow(() -> new ResourceNotFoundException("Plan", "Free")));

    return ResponseEntity.ok(
        new PlanResponse(
            plan.getId(),
            plan.getName(),
            plan.getMaxProjects(),
            plan.getMaxTokensPerDay(),
            plan.getUnlimitedAI(),
            plan.getPrice()));
  }
}
