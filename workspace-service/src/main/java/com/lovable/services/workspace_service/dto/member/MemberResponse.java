package com.lovable.services.workspace_service.dto.member;


import com.lovable.services.common_lib.enums.ProjectRole;

import java.time.Instant;

public record MemberResponse(
        Long userId, String userName, ProjectRole projectRole, Instant invitedAt, Instant AcceptedAt) {}
