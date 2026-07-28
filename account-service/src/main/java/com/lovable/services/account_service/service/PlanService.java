package com.lovable.services.account_service.service;


import com.lovable.services.common_lib.dto.PlanResponse;

import java.util.List;

public interface PlanService {
  List<PlanResponse> getPlans();
}
