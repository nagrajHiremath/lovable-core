package com.lovable.services.intelligence_service.dto;

public record UsageTodayResponse(int tokensUsedToday, int maxTokensPerDay, boolean unlimitedAi) {}
