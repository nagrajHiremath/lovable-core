package com.lovable.services.workspace_service.service.impl;

import com.lovable.services.common_lib.dto.FileNode;
import com.lovable.services.common_lib.dto.FileTreeResponse;
import com.lovable.services.common_lib.exception.ResourceNotFoundException;
import com.lovable.services.workspace_service.entity.Project;
import com.lovable.services.workspace_service.entity.ProjectFile;
import com.lovable.services.workspace_service.mapper.ProjectFileMapper;
import com.lovable.services.workspace_service.repository.ProjectFileRepository;
import com.lovable.services.workspace_service.repository.ProjectRepository;
import com.lovable.services.workspace_service.service.ProjectFileService;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectFileServiceImpl implements ProjectFileService {

  private final ProjectFileRepository projectFileRepository;
  private final ProjectFileMapper projectFileMapper;
  private final ProjectRepository projectRepository;
  private final MinioClient minioClient;

  @Value("${minio.project-bucket}")
  private String projectBucket;

  public FileTreeResponse getFileTree(Long projectId) {

      List<ProjectFile> projectFileList = projectFileRepository.findByProjectId(projectId);
      List<FileNode> fileNodeList = projectFileMapper.toFileNodeList(projectFileList);

      return new FileTreeResponse(fileNodeList);
  }


  @Override
  public String getFileContent(Long projectId, String path) {
    String objectName = projectId + "/" + path;
    try (
            InputStream is = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(projectBucket)
                            .object(objectName)
                            .build())) {

      String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//      return new FileContentResponse(path, content);
      return content;
    } catch (Exception e) {
      log.error("Failed to read file: {}/{}", projectId, path, e);
      throw new RuntimeException("Failed to read file content", e);
    }
  }

  @Override
  public void saveFile(Long projectId, String path, String content) {
    Project project = projectRepository.findById(projectId).orElseThrow(
            () -> new ResourceNotFoundException("Project", projectId.toString())
    );

    String cleanPath = path.startsWith("/") ? path.substring(1) : path;
    String objectKey = projectId + "/" + cleanPath;

    try {
      byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
      InputStream inputStream = new ByteArrayInputStream(contentBytes);
      // saving the file content
      minioClient.putObject(
              PutObjectArgs.builder()
                      .bucket(projectBucket)
                      .object(objectKey)
                      .stream(inputStream, contentBytes.length, -1)
                      .contentType(determineContentType(path))
                      .build());

      // Saving the metaData
      ProjectFile file = projectFileRepository.findByProjectIdAndPath(projectId, cleanPath)
              .orElseGet(() -> ProjectFile.builder()
                      .project(project)
                      .path(cleanPath)
                      .minioObjectKey(objectKey) // Use the key we generated
                      .createdAt(Instant.now())
                      .build());

      file.setUpdatedAt(Instant.now());
      projectFileRepository.save(file);
      log.info("Saved file: {}", objectKey);
    } catch (Exception e) {
      log.error("Failed to save file {}/{}", projectId, cleanPath, e);
      throw new RuntimeException("File save failed", e);
    }

  }

  @Override
  public byte[] downloadProjectAsZip(Long projectId) {
    List<ProjectFile> projectFileList = projectFileRepository.findByProjectId(projectId);

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      for (ProjectFile projectFile : projectFileList) {
        zipOutputStream.putNextEntry(new ZipEntry(projectFile.getPath()));
        try (InputStream is =
            minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(projectBucket)
                    .object(projectFile.getMinioObjectKey())
                    .build())) {
          is.transferTo(zipOutputStream);
        }
        zipOutputStream.closeEntry();
      }
    } catch (Exception e) {
      log.error("Failed to build zip for project: {}", projectId, e);
      throw new RuntimeException("Failed to build project zip", e);
    }

    return byteArrayOutputStream.toByteArray();
  }

  @Override
  public void deleteProjectFiles(Long projectId) {
    String prefix = projectId + "/";

    Iterable<Result<Item>> objects = minioClient.listObjects(
        ListObjectsArgs.builder()
            .bucket(projectBucket)
            .prefix(prefix)
            .recursive(true)
            .build());

    for (Result<Item> result : objects) {
      try {
        String objectName = result.get().objectName();
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(projectBucket)
                .object(objectName)
                .build());
      } catch (Exception e) {
        log.error("Failed to delete an object for project {}", projectId, e);
      }
    }

    projectFileRepository.deleteAll(projectFileRepository.findByProjectId(projectId));
  }

  private String determineContentType(String path) {
    String type = URLConnection.guessContentTypeFromName(path);
    if (type != null) return type;
    if (path.endsWith(".jsx") || path.endsWith(".ts") || path.endsWith(".tsx")) return "text/javascript";
    if (path.endsWith(".json")) return "application/json";
    if (path.endsWith(".css")) return "text/css";

    return "text/plain";
  }
}
