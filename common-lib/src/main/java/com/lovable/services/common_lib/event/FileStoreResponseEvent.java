package com.lovable.services.common_lib.event;

import lombok.Builder;

@Builder
public record FileStoreResponseEvent(
        String idempotencyKey,
        boolean success,
        String errorMessage,
        Long projectId
) {
}
