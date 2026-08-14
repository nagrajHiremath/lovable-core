package com.lovable.services.account_service.service.impl;

import com.lovable.services.account_service.dto.auth.AuthResponse;
import com.lovable.services.account_service.dto.auth.LoginRequest;
import com.lovable.services.account_service.dto.auth.RefreshRequest;
import com.lovable.services.account_service.dto.auth.SignUpRequest;
import com.lovable.services.account_service.entity.User;
import com.lovable.services.account_service.mapper.AuthMapper;
import com.lovable.services.account_service.repository.UserRepository;
import com.lovable.services.account_service.service.AuthService;
import com.lovable.services.common_lib.dto.UserProfileResponse;
import com.lovable.services.common_lib.exception.BadRequestException;
import com.lovable.services.common_lib.exception.ResourceNotFoundException;
import com.lovable.services.common_lib.security.AuthUtil;
import com.lovable.services.common_lib.security.JwtUserPrincipal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

  UserRepository userRepository;
  AuthMapper authMapper;
  AuthUtil authUtil;
  PasswordEncoder passwordEncoder;
  AuthenticationManager authenticationManager;

  public AuthResponse signUp(SignUpRequest signUpRequest) {

    Optional<User> user = userRepository.findByUsername(signUpRequest.username());

    if (user.isPresent()) {
      throw new BadRequestException("User already exist: " + user.get().getUsername());
    }

    User newUser = authMapper.toUserEntity(signUpRequest);
    newUser.setPassword(passwordEncoder.encode(signUpRequest.password()));
    userRepository.save(newUser);

    return buildAuthResponse(newUser);
  }

  public AuthResponse login(LoginRequest loginRequest) {

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.username(), loginRequest.password()));

    User user = (User) authentication.getPrincipal();
    return buildAuthResponse(user);
  }

  public AuthResponse refresh(RefreshRequest refreshRequest) {
    JwtUserPrincipal principal = authUtil.verifyRefreshToken(refreshRequest.refreshToken());

    User user =
        userRepository
            .findById(principal.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User", String.valueOf(principal.userId())));

    return buildAuthResponse(user);
  }

  private AuthResponse buildAuthResponse(User user) {
    JwtUserPrincipal principal = authMapper.toJwtUserPrincipal(user);
    return new AuthResponse(
        authUtil.generateAccessToken(principal),
        authUtil.generateRefreshToken(principal),
        authMapper.toUserProfileResponse(user));
  }

  public UserProfileResponse getCurrentProfile() {
    long userId = authUtil.getCurrentUserId();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", String.valueOf(userId)));
    return authMapper.toUserProfileResponse(user);
  }
}
