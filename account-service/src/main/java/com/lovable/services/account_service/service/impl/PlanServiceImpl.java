package com.lovable.services.account_service.service.impl;

import com.lovable.services.account_service.entity.Plan;
import com.lovable.services.account_service.repository.PlanRepository;
import com.lovable.services.account_service.service.PlanService;
import com.lovable.services.common_lib.dto.PlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

  private final PlanRepository planRepository;

  public List<PlanResponse> getPlans() {
    return planRepository.findAll().stream()
        .filter(Plan::getIsActive)
        .map(this::toPlanResponse)
        .toList();
  }

  private PlanResponse toPlanResponse(Plan plan) {
    return new PlanResponse(
        plan.getId(),
        plan.getName(),
        plan.getMaxProjects(),
        plan.getMaxTokensPerDay(),
        plan.getUnlimitedAI(),
        plan.getPrice());
  }
}
