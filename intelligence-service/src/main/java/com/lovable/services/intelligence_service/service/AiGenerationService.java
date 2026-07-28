package com.lovable.services.intelligence_service.service;


import com.lovable.services.intelligence_service.dto.StreamResponse;
import reactor.core.publisher.Flux;

public interface AiGenerationService {
  Flux<StreamResponse> streamResponse(String message, Long projectId);
}
