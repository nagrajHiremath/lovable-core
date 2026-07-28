package com.lovable.services.workspace_service.controller;

import com.lovable.services.common_lib.dto.FileTreeResponse;
import com.lovable.services.workspace_service.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/projects/{projectId}/files")
public class FileController {

  private final ProjectFileService projectFileService;

  @GetMapping
  public ResponseEntity<FileTreeResponse> getFileTree(@PathVariable Long projectId) {
    return ResponseEntity.ok(projectFileService.getFileTree(projectId));
  }

  @GetMapping("/content")
  public ResponseEntity<String> getFile(@PathVariable Long projectId,
                                                     @RequestParam String path) {
    return ResponseEntity.ok(projectFileService.getFileContent(projectId, path));
  }
}
