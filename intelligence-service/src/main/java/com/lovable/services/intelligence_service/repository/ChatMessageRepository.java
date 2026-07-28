package com.lovable.services.intelligence_service.repository;

import com.lovable.services.intelligence_service.entity.ChatMessage;
import com.lovable.services.intelligence_service.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatSession(ChatSession chatSession);
}
