package com.lovable.services.account_service.service;


import com.lovable.services.account_service.dto.auth.AuthResponse;
import com.lovable.services.account_service.dto.auth.LoginRequest;
import com.lovable.services.account_service.dto.auth.SignUpRequest;
import com.lovable.services.common_lib.dto.UserProfileResponse;

public interface AuthService {
  AuthResponse login(LoginRequest loginRequest);

  AuthResponse signUp(SignUpRequest signUpRequest);

  UserProfileResponse getCurrentProfile();
}
