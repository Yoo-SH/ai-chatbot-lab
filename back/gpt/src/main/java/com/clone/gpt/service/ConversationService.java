package com.clone.gpt.service;

import com.clone.gpt.model.dto.request.conversation.ConversationRequest;
import com.clone.gpt.model.dto.response.conversation.ConversationResponse;
import com.clone.gpt.model.dto.response.message.MessageResponse;
import com.clone.gpt.model.entity.Conversation;
import com.clone.gpt.model.entity.Message;
import com.clone.gpt.model.entity.User;
import com.clone.gpt.repository.ConversationRepository;
import com.clone.gpt.repository.MessageRepository;
import com.clone.gpt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConversationService {
    
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    
    public ConversationResponse createConversation(Long userId, ConversationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Conversation conversation = Conversation.builder()
                .user(user)
                .title(request.getTitle())
                .build();
        
        Conversation savedConversation = conversationRepository.save(conversation);
        
        return convertToConversationResponse(savedConversation);
    }
    
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        
        return conversations.stream()
                .map(this::convertToConversationResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ConversationResponse getConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("대화를 찾을 수 없습니다."));
        
        return convertToConversationResponseWithMessages(conversation);
    }
    
    public ConversationResponse updateConversation(Long userId, Long conversationId, ConversationRequest request) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("대화를 찾을 수 없습니다."));
        
        conversation.setTitle(request.getTitle());
        Conversation updatedConversation = conversationRepository.save(conversation);
        
        return convertToConversationResponse(updatedConversation);
    }
    
    public void deleteConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("대화를 찾을 수 없습니다."));
        
        conversationRepository.delete(conversation);
    }
    
    private ConversationResponse convertToConversationResponse(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
    
    private ConversationResponse convertToConversationResponseWithMessages(Conversation conversation) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        
        List<MessageResponse> messageResponses = messages.stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());
        
        return ConversationResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .messages(messageResponses)
                .build();
    }
    
    private MessageResponse convertToMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .parentId(message.getParentId())
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
} 