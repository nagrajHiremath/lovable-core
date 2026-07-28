package com.lovable.services.intelligence_service.service;





import com.lovable.services.intelligence_service.dto.ChatResponse;

import java.util.List;

public interface ChatService {
    List<ChatResponse> getProjectChatHistory(Long projectId);
}
