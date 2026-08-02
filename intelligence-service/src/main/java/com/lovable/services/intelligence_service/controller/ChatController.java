package com.lovable.services.intelligence_service.controller;

import com.lovable.services.intelligence_service.dto.ChatRequest;
import com.lovable.services.intelligence_service.dto.ChatResponse;
import com.lovable.services.intelligence_service.dto.StreamResponse;
import com.lovable.services.intelligence_service.service.AiGenerationService;
import com.lovable.services.intelligence_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

  private final AiGenerationService aiGenerationService;
  private final ChatService chatService;

  @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<StreamResponse> streamChat(@RequestBody ChatRequest request) {

    // aiGenerationService.streamResponse already produces ServerSentEvent<String>,
    // so return the flux directly instead of wrapping it again.
    return aiGenerationService.streamResponse(request.message(), request.projectId());
  }

  @GetMapping("/projects/{projectId}")
  public ResponseEntity<List<ChatResponse>> getChatHistory(
          @PathVariable Long projectId) {

    return ResponseEntity.ok(chatService.getProjectChatHistory(projectId));
  }
}
