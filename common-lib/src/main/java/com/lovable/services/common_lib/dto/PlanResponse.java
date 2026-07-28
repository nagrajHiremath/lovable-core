package com.lovable.services.common_lib.dto;

public record PlanResponse(
    Long id,
    String name,
    Integer maxProjects,
    Integer maxTokensPerDay,
    Boolean unlimitedAi,
    String price) {}
