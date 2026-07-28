package com.lovable.services.intelligence_service.consumer;

import com.lovable.services.common_lib.enums.ChatEventStatus;
import com.lovable.services.common_lib.event.FileStoreResponseEvent;
import com.lovable.services.intelligence_service.repository.ChatEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class IntelligenceIdempotencyConsumer {
    private final ChatEventRepository chatEventRepository;

    @Transactional
    @KafkaListener(topics = "file-store-responses", groupId = "intelligence-group")
    public void handleIdempotency(FileStoreResponseEvent response) {

        chatEventRepository.findByIdempotencyKey(response.idempotencyKey()).ifPresent(chatEvent -> {

            if (!ChatEventStatus.PENDING.equals(chatEvent.getStatus())) { //Idempotency
                log.info("Response for Saga {} already handled. Skipping.", response.idempotencyKey());
                return;
            }

            if (response.success()) {
                chatEvent.setStatus(ChatEventStatus.CONFIRMED);
                log.info("Saga {} CONFIRMED", response.idempotencyKey());
            } else {
                log.warn("Saga {} FAILED. Deleting event.", response.idempotencyKey());
                chatEvent.setStatus(ChatEventStatus.FAILED);
            }
        });
    }
}
