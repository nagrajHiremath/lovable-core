package com.lovable.services.account_service.service.impl;

import com.lovable.services.account_service.dto.auth.AuthResponse;
import com.lovable.services.account_service.dto.auth.LoginRequest;
import com.lovable.services.account_service.dto.auth.SignUpRequest;
import com.lovable.services.account_service.entity.User;
import com.lovable.services.account_service.mapper.AuthMapper;
import com.lovable.services.account_service.repository.UserRepository;
import com.lovable.services.account_service.service.AuthService;
import com.lovable.services.common_lib.exception.BadRequestException;
import com.lovable.services.common_lib.security.AuthUtil;
import jakarta.servlet.http.Cookie;
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

    String accessToken = authUtil.generateAccessToken(authMapper.toJwtUserPrincipal(newUser));
    Cookie cookie = new Cookie("refreshToken", authUtil.generateRefreshToken(authMapper.toJwtUserPrincipal(newUser)));

    return new AuthResponse(accessToken, authMapper.toUserProfileResponse(newUser));
  }

  public AuthResponse login(LoginRequest loginRequest) {

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.username(), loginRequest.password()));

    User user = (User) authentication.getPrincipal();
    return new AuthResponse(
        authUtil.generateAccessToken(authMapper.toJwtUserPrincipal(user)), authMapper.toUserProfileResponse(user));
  }

  public SignUpRequest getCurrentProfile() {
    return null;
  }
}
