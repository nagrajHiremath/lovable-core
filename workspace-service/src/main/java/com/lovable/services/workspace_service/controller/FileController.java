package com.lovable.services.workspace_service.controller;

import com.lovable.services.common_lib.dto.FileTreeResponse;
import com.lovable.services.workspace_service.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/files")
public class FileController {

  private final ProjectFileService projectFileService;

  @GetMapping("/tree")
  public ResponseEntity<FileTreeResponse> getFileTree(@PathVariable Long projectId) {
    return ResponseEntity.ok(projectFileService.getFileTree(projectId));
  }

  @GetMapping("/content")
  public ResponseEntity<String> getFile(@PathVariable Long projectId,
                                                     @RequestParam String path) {
    return ResponseEntity.ok(projectFileService.getFileContent(projectId, path));
  }

  @GetMapping("/download-zip")
  public ResponseEntity<byte[]> downloadZip(@PathVariable Long projectId) {
    byte[] zipBytes = projectFileService.downloadProjectAsZip(projectId);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"project-" + projectId + ".zip\"")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(zipBytes);
  }
}
