package com.lovable.services.intelligence_service.repository;

import com.lovable.services.intelligence_service.entity.ChatSession;
import com.lovable.services.intelligence_service.entity.ChatSessionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, ChatSessionId> {}
