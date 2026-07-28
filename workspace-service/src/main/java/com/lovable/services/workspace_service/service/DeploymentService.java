package com.lovable.services.workspace_service.service;


import com.lovable.services.workspace_service.dto.project.DeployResponse;

public interface DeploymentService {

    DeployResponse deploy(Long projectId);
}