package com.lovable.services.workspace_service.service;

import com.lovable.services.common_lib.dto.FileTreeResponse;

public interface ProjectFileService {
  FileTreeResponse getFileTree(Long projectId);


  String getFileContent(Long projectId, String cleanPath);

  void saveFile(Long projectId, String filePath, String content);
}
