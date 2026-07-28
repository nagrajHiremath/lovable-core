package com.lovable.services.account_service.mapper;

import com.lovable.services.account_service.dto.auth.SignUpRequest;
import com.lovable.services.account_service.entity.User;
import com.lovable.services.common_lib.dto.UserProfileResponse;
import com.lovable.services.common_lib.security.JwtUserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

  @Mapping(source = "username", target = "username")
  User toUserEntity(SignUpRequest signUpRequest);

  UserProfileResponse toUserProfileResponse(User newUser);

  JwtUserPrincipal toJwtUserPrincipal(User newUser);
}
