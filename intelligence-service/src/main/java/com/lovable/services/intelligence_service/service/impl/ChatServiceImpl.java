package com.lovable.services.intelligence_service.service.impl;

import com.lovable.services.common_lib.security.AuthUtil;
import com.lovable.services.intelligence_service.dto.ChatResponse;
import com.lovable.services.intelligence_service.entity.ChatMessage;
import com.lovable.services.intelligence_service.entity.ChatSession;
import com.lovable.services.intelligence_service.entity.ChatSessionId;
import com.lovable.services.intelligence_service.mapper.ChatMapper;
import com.lovable.services.intelligence_service.repository.ChatMessageRepository;
import com.lovable.services.intelligence_service.repository.ChatSessionRepository;
import com.lovable.services.intelligence_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

    private final AuthUtil authUtil;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMapper chatMapper;

    @Override
    public List<ChatResponse> getProjectChatHistory(Long projectId) {

        Long userId = authUtil.getCurrentUserId();

        ChatSession chatSession = chatSessionRepository.getReferenceById(
                new ChatSessionId(projectId, userId)
        );

        List<ChatMessage> chatMessageList = chatMessageRepository.findByChatSession(chatSession);

        return chatMapper.toListOfChatResponse(chatMessageList);
    }
}
