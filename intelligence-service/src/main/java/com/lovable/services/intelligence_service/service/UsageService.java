package com.lovable.services.intelligence_service.service;

import com.lovable.services.intelligence_service.dto.UsageTodayResponse;

public interface UsageService {

  UsageTodayResponse getTodayUsage();

  void recordTokenUsage(Long id, int totalTokens);

  void checkDailyTokensUsage();
}
