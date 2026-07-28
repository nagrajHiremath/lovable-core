package com.lovable.services.account_service.dto.subscription;


import com.lovable.services.common_lib.dto.PlanResponse;

import java.time.Instant;

public record SubscriptionResponse(
        PlanResponse plan, String status, Instant currentPeriodEnd, Long tokensUsedThisCycle) {}
