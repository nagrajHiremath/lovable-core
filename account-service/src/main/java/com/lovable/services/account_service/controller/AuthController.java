package com.lovable.services.account_service.controller;

import com.lovable.services.account_service.dto.auth.AuthResponse;
import com.lovable.services.account_service.dto.auth.LoginRequest;
import com.lovable.services.account_service.dto.auth.RefreshRequest;
import com.lovable.services.account_service.dto.auth.SignUpRequest;
import com.lovable.services.account_service.service.AuthService;
import com.lovable.services.common_lib.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest signUpRequest) {
    return ResponseEntity.ok(authService.signUp(signUpRequest));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
    return ResponseEntity.ok(authService.login(loginRequest));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest refreshRequest) {
    return ResponseEntity.ok(authService.refresh(refreshRequest));
  }

  @GetMapping("/profile")
  public ResponseEntity<UserProfileResponse> getCurrentProfile() {
    return ResponseEntity.ok(authService.getCurrentProfile());
  }
}
