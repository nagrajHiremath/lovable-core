package com.lovable.services.intelligence_service.controller;

import com.lovable.services.intelligence_service.dto.UsageTodayResponse;
import com.lovable.services.intelligence_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usage")
public class UsageController {

  private final UsageService usageService;

  @GetMapping("/today")
  public ResponseEntity<UsageTodayResponse> getTodayUsage() {
    return ResponseEntity.ok(usageService.getTodayUsage());
  }
}
