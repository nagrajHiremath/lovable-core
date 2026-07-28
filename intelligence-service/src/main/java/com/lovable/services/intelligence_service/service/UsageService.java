package com.lovable.services.intelligence_service.service;


public interface UsageService {
//  UsageTodayResponse getTodayUsage();
//
//  PlanLimitResponse getPlanLimit();

  void recordTokenUsage(Long id, int totalTokens);

  void checkDailyTokensUsage();
}
