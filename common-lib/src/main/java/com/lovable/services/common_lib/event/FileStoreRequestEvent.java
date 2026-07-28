package com.lovable.services.common_lib.event;

public record FileStoreRequestEvent(
        Long projectId,
        String idempotencyKey,
        String filePath,
        String content,
        Long userId
) {
}
