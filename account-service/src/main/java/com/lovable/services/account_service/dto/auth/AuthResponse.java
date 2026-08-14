package com.lovable.services.account_service.dto.auth;

import com.lovable.services.common_lib.dto.UserProfileResponse;

public record AuthResponse(String token, String refreshToken, UserProfileResponse userProfileResponse) {}
