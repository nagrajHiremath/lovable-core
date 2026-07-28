package com.lovable.services.workspace_service.consumer;

import com.lovable.services.common_lib.event.FileStoreRequestEvent;
import com.lovable.services.common_lib.event.FileStoreResponseEvent;
import com.lovable.services.workspace_service.entity.ProcessedEvent;
import com.lovable.services.workspace_service.repository.ProcessedEventRepository;
import com.lovable.services.workspace_service.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class FileStoreConsumer {

    private final ProjectFileService projectFileService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "file-store-request-event", groupId = "workspace-group")
    public void consumeEvent(FileStoreRequestEvent request){

        try {
            log.info("Saving file: {}", request.filePath());

            projectFileService.saveFile(request.projectId(), request.filePath(), request.content());
            processedEventRepository.save(new ProcessedEvent(request.idempotencyKey(), LocalDateTime.now()));

            sendEventResponse(request, true, null);

        } catch (Exception e) {
            log.error("Error saving file: {}", e.getMessage());
            sendEventResponse(request, false, e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private void sendEventResponse(FileStoreRequestEvent requestEvent, boolean success, String error){

        FileStoreResponseEvent responseEvent = FileStoreResponseEvent.builder()
                .idempotencyKey(requestEvent.idempotencyKey())
                .success(success)
                .errorMessage(error)
                .projectId(requestEvent.projectId())
                .build();

    }
}
