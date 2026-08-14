package com.lovable.services.intelligence_service.security;

import com.lovable.services.common_lib.enums.ProjectPermission;
import com.lovable.services.common_lib.security.AuthUtil;
import com.lovable.services.intelligence_service.client.WorkspaceClient;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Component;

@Component("security")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SecurityExpression {

    WorkspaceClient workspaceClient;
    AuthUtil authUtil;

    public boolean hasPermissions(long projectId, ProjectPermission permission) {
        try {
            long userId = authUtil.getCurrentUserId();
            return workspaceClient.checkPermission(projectId, userId, permission);
        } catch (FeignException.Unauthorized e) {
            log.warn("Token expired or invalid during permission check for project: {}", projectId);
            throw new CredentialsExpiredException("JWT token is expired or invalid");
        } catch (FeignException e) {
            log.error("Workspace-service failed during permission check: {}", e.getMessage());
            return false;
        }
    }

    public boolean canViewProject(long projectId, ProjectPermission permission) {
        return hasPermissions(projectId, permission);
    }

    public boolean canDeleteProject(long projectId, ProjectPermission permission) {
        return hasPermissions(projectId, permission);
    }

    public boolean canEditProject(long projectId, ProjectPermission permission) {
        return hasPermissions(projectId, permission);
    }
}
