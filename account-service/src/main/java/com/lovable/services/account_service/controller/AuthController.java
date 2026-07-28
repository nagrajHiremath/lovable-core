package com.lovable.services.account_service.controller;

import com.lovable.services.account_service.dto.auth.AuthResponse;
import com.lovable.services.account_service.dto.auth.LoginRequest;
import com.lovable.services.account_service.dto.auth.SignUpRequest;
import com.lovable.services.account_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signUp")
  public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest signUpRequest) {
    return ResponseEntity.ok(authService.signUp(signUpRequest));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
    return ResponseEntity.ok(authService.login(loginRequest));
  }

  @GetMapping("/profile")
  public ResponseEntity<SignUpRequest> getCurrentProfile() {
    return ResponseEntity.ok(authService.getCurrentProfile());
  }
}
