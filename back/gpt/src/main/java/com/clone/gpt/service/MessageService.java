package com.clone.gpt.service;

import com.clone.gpt.model.dto.request.message.MessageRequest;
import com.clone.gpt.model.dto.response.message.MessageResponse;
import com.clone.gpt.model.entity.Conversation;
import com.clone.gpt.model.entity.Message;
import com.clone.gpt.model.entity.Message.MessageRole;
import com.clone.gpt.repository.ConversationRepository;
import com.clone.gpt.repository.MessageRepository;
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
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    
    public MessageResponse sendMessage(Long userId, MessageRequest request) {
        // 대화 존재 및 권한 확인
        Conversation conversation = conversationRepository.findByIdAndUserId(request.getConversationId(), userId)
                .orElseThrow(() -> new RuntimeException("대화를 찾을 수 없습니다."));
        
        // 사용자 메시지 저장
        Message userMessage = Message.builder()
                .conversation(conversation)
                .parentId(request.getParentId())
                .role(MessageRole.USER)
                .content(request.getContent())
                .build();
        
        Message savedUserMessage = messageRepository.save(userMessage);
        
        // 대화 업데이트 시간 갱신
        conversationRepository.save(conversation);
        
        return convertToMessageResponse(savedUserMessage);
    }
    
    public MessageResponse sendAssistantMessage(Long userId, Long conversationId, String content, Long parentId) {
        // 대화 존재 및 권한 확인
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("대화를 찾을 수 없습니다."));
        
        // AI 응답 메시지 저장
        Message assistantMessage = Message.builder()
                .conversation(conversation)
                .parentId(parentId)
                .role(MessageRole.ASSISTANT)
                .content(content)
                .build();
        
        Message savedAssistantMessage = messageRepository.save(assistantMessage);
        
        // 대화 업데이트 시간 갱신
        conversationRepository.save(conversation);
        
        return convertToMessageResponse(savedAssistantMessage);
    }
    
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(Long userId, Long conversationId) {
        // 권한 확인
        conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new RuntimeException("대화를 찾을 수 없습니다."));
        
        List<Message> messages = messageRepository.findByConversationIdAndUserIdOrderByCreatedAtAsc(conversationId, userId);
        
        return messages.stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());
    }
    
    public void deleteMessage(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("메시지를 찾을 수 없습니다."));
        
        // 권한 확인
        if (!message.getConversation().getUser().getId().equals(userId)) {
            throw new RuntimeException("메시지를 삭제할 권한이 없습니다.");
        }
        
        messageRepository.delete(message);
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