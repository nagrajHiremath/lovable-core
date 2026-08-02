package com.lovable.services.workspace_service.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioStarterTemplateInitializer implements ApplicationRunner {

  private static final String TEMPLATE_BUCKET = "starter-projects";
  private static final String TEMPLATE_NAME = "react-vite-tailwind-daisyui-starter";
  private static final String RESOURCE_GLOB = "classpath*:react-vite-tailwind-daisyui-starter/**";
  private static final int MAX_ATTEMPTS = 10;
  private static final long RETRY_DELAY_MS = 3000L;

  private final MinioClient minioClient;

  @Value("${minio.project-bucket}")
  private String projectBucket;

  @Override
  public void run(ApplicationArguments args) {
    Exception lastException = null;

    for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
      try {
        ensureBucketExists(projectBucket);
        ensureBucketExists(TEMPLATE_BUCKET);
        seedStarterTemplate();
        log.info("MinIO starter template initialization completed successfully.");
        return;
      } catch (Exception ex) {
        lastException = ex;
        log.warn(
            "MinIO starter template initialization attempt {}/{} failed: {}",
            attempt,
            MAX_ATTEMPTS,
            ex.getMessage());

        if (attempt < MAX_ATTEMPTS) {
          try {
            Thread.sleep(RETRY_DELAY_MS);
          } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry MinIO initialization", interruptedException);
          }
        }
      }
    }

    throw new IllegalStateException("Failed to initialize MinIO starter template", lastException);
  }

  private void ensureBucketExists(String bucketName) throws Exception {
    boolean exists =
        minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

    if (!exists) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
      log.info("Created MinIO bucket: {}", bucketName);
    }
  }

  private void seedStarterTemplate() throws Exception {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources(RESOURCE_GLOB);

    for (Resource resource : resources) {
      if (!resource.isReadable()) {
        continue;
      }

      String relativePath = extractRelativePath(resource);
      if (relativePath == null || relativePath.isBlank() || relativePath.endsWith("/")) {
        continue;
      }

      String objectName = TEMPLATE_NAME + "/" + relativePath;

      try (InputStream inputStream = resource.getInputStream()) {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(TEMPLATE_BUCKET)
                .object(objectName)
                .stream(inputStream, resource.contentLength(), -1)
                .build());
      }
    }

    log.info("Seeded starter template '{}' into bucket '{}'.", TEMPLATE_NAME, TEMPLATE_BUCKET);
  }

  private String extractRelativePath(Resource resource) throws Exception {
    String normalizedUrl = resource.getURL().toString().replace('\\', '/');
    String marker = TEMPLATE_NAME + "/";
    int markerIndex = normalizedUrl.indexOf(marker);

    if (markerIndex < 0) {
      return null;
    }

    return normalizedUrl.substring(markerIndex + marker.length());
  }
}

