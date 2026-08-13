package com.lovable.services.account_service.controller;

import com.lovable.services.account_service.entity.User;
import com.lovable.services.account_service.mapper.AuthMapper;
import com.lovable.services.account_service.repository.UserRepository;
import com.lovable.services.common_lib.dto.UserProfileResponse;
import com.lovable.services.common_lib.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Service-to-service endpoints, not exposed through the API gateway.
 * Used by workspace-service (project member invites) and other internal callers
 * that need to resolve a user by email or id without going through public auth.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/users")
public class InternalUserController {

  private final UserRepository userRepository;
  private final AuthMapper authMapper;

  @GetMapping("/by-email")
  public ResponseEntity<UserProfileResponse> getUserByEmail(@RequestParam("email") String email) {
    User user =
        userRepository
            .findByUsername(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));
    return ResponseEntity.ok(authMapper.toUserProfileResponse(user));
  }

  @GetMapping("/by-ids")
  public ResponseEntity<List<UserProfileResponse>> getUsersByIds(@RequestParam("ids") List<Long> ids) {
    List<User> users = userRepository.findAllById(ids);
    return ResponseEntity.ok(users.stream().map(authMapper::toUserProfileResponse).toList());
  }
}
