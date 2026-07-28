package com.lovable.services.intelligence_service.mapper;

import com.lovable.services.intelligence_service.dto.ChatResponse;
import com.lovable.services.intelligence_service.entity.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    ChatResponse toChatResponse(ChatMessage chatMessage);
    List<ChatResponse> toListOfChatResponse(List<ChatMessage> chatMessageList);
}
