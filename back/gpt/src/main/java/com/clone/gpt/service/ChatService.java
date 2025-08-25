package com.clone.gpt.service;

import com.clone.gpt.model.dto.request.chat.ChatRequest;
import com.clone.gpt.model.dto.response.chat.ChatResponse;
import com.clone.gpt.model.dto.response.ai.AIResponse;
import com.clone.gpt.model.dto.response.ai.AIStreamResponse;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final AIService aiService; // AI 서비스 추가
    
    // SSE Emitter 관리
    private final ConcurrentMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ChatStreamContext> streamContexts = new ConcurrentHashMap<>();
    
    /**
     * 일반 HTTP 방식 - 전체 응답을 한 번에 처리
     */
    public ChatResponse processMessage(Long userId, ChatRequest request) {
        try {
            // 사용자 검증
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            // 대화 검증
            Conversation conversation = conversationRepository.findByIdAndUserId(request.getConversationId(), userId)
                    .orElseThrow(() -> new RuntimeException("대화를 찾을 수 없습니다."));
            
            // 사용자 메시지 저장
            Message userMessage = Message.builder()
                    .conversation(conversation)
                    .role(Message.MessageRole.USER)
                    .content(request.getMessage())
                    .parentId(request.getParentId())
                    .build();
            userMessage = messageRepository.save(userMessage);
            
            // 대화 기록 조회 (최근 10개 메시지)
            List<Message> conversationHistory = messageRepository
                    .findByConversationIdOrderByCreatedAtDesc(conversation.getId())
                    .stream()
                    .limit(10)
                    .toList();
            
            // AI 서비스 호출
            log.info("AI 서비스 호출 - 사용자 ID: {}, 메시지: {}", userId, request.getMessage());
            AIResponse aiResponse = aiService.chat(
                request.getMessage(), 
                conversationHistory, 
                "당신은 도움이 되는 AI 어시스턴트입니다."
            );
            
            // AI 응답 메시지 저장
            Message aiMessage = Message.builder()
                    .conversation(conversation)
                    .role(Message.MessageRole.ASSISTANT)
                    .content(aiResponse.getResponse())
                    .parentId(userMessage.getId())
                    .build();
            aiMessage = messageRepository.save(aiMessage);
            
            // 대화 업데이트 시간 갱신
            conversation.setUpdatedAt(LocalDateTime.now());
            conversationRepository.save(conversation);
            
            return ChatResponse.builder()
                    .messageId(aiMessage.getId())
                    .content(aiResponse.getResponse())
                    .role("assistant")
                    .timestamp(aiMessage.getCreatedAt())
                    .conversationId(conversation.getId())
                    .build();
            
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("메시지 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * SSE 스트리밍 방식 - 스트리밍 시작
     */
    public String startStreaming(Long userId, ChatRequest request) {
        try {
            // 사용자 검증
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            // 대화 검증
            Conversation conversation = conversationRepository.findByIdAndUserId(request.getConversationId(), userId)
                    .orElseThrow(() -> new RuntimeException("대화를 찾을 수 없습니다."));
            
            // 사용자 메시지 저장
            Message userMessage = Message.builder()
                    .conversation(conversation)
                    .role(Message.MessageRole.USER)
                    .content(request.getMessage())
                    .parentId(request.getParentId())
                    .build();
            userMessage = messageRepository.save(userMessage);
            
            // 스트리밍 ID 생성
            String streamId = UUID.randomUUID().toString();
            
            // 대화 기록 조회
            List<Message> conversationHistory = messageRepository
                    .findByConversationIdOrderByCreatedAtDesc(conversation.getId())
                    .stream()
                    .limit(10)
                    .toList();
            
            // AI 스트리밍 시작
            String aiStreamId = aiService.startStreamingChat(
                request.getMessage(),
                conversationHistory,
                "당신은 도움이 되는 AI 어시스턴트입니다."
            );
            
            // 스트리밍 컨텍스트 저장
            ChatStreamContext context = new ChatStreamContext();
            context.setUserId(userId);
            context.setConversationId(conversation.getId());
            context.setUserMessageId(userMessage.getId());
            context.setAiStreamId(aiStreamId);
            context.setContent(new StringBuilder());
            
            streamContexts.put(streamId, context);
            
            log.info("스트리밍 시작 - Stream ID: {}, AI Stream ID: {}", streamId, aiStreamId);
            return streamId;
            
        } catch (Exception e) {
            log.error("스트리밍 시작 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("스트리밍 시작 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * SSE Emitter 등록 및 스트리밍 시작
     */
    public void registerEmitter(String streamId, SseEmitter emitter) {
        emitters.put(streamId, emitter);
        
        ChatStreamContext context = streamContexts.get(streamId);
        if (context == null) {
            log.error("스트리밍 컨텍스트를 찾을 수 없습니다: {}", streamId);
            try {
                emitter.send(SseEmitter.event()
                        .name("chunk")
                        .data("{\"type\":\"ERROR\",\"error\":\"스트리밍 컨텍스트 오류\"}"));
                emitter.complete();
            } catch (Exception e) {
                log.error("오류 메시지 전송 실패: {}", e.getMessage());
            }
            removeEmitter(streamId);
            return;
        }
        
        // 비동기로 AI 응답 처리
        CompletableFuture.runAsync(() -> {
            try {
                // 대화 기록 조회
                List<Message> conversationHistory = messageRepository
                        .findByConversationIdOrderByCreatedAtDesc(context.getConversationId())
                        .stream()
                        .limit(10)
                        .toList();
                
                // 사용자 메시지 조회
                Message userMessage = messageRepository.findById(context.getUserMessageId())
                        .orElseThrow(() -> new RuntimeException("사용자 메시지를 찾을 수 없습니다."));
                
                // 시작 이벤트 전송
                sendSSEEvent(streamId, "START", "", null);
                
                // AI 응답 요청
                AIResponse aiResponse = aiService.chat(
                    userMessage.getContent(),
                    conversationHistory,
                    "당신은 도움이 되는 AI 어시스턴트입니다."
                );
                
                // 응답을 조각으로 나누어 스트리밍처럼 전송
                String fullResponse = aiResponse.getResponse();
                simulateStreamingResponse(streamId, fullResponse);
                
                // AI 응답 메시지 저장
                saveAIMessage(context, fullResponse);
                
                // 완료 이벤트 전송
                sendSSEEvent(streamId, "END", fullResponse, null);
                
            } catch (Exception e) {
                log.error("스트리밍 처리 오류: {}", e.getMessage(), e);
                sendSSEEvent(streamId, "ERROR", "", e.getMessage());
            } finally {
                // 연결 완료
                handleStreamComplete(streamId);
            }
        });
    }
    
    /**
     * SSE 이벤트 전송
     */
    private void sendSSEEvent(String streamId, String type, String content, String error) {
        SseEmitter emitter = emitters.get(streamId);
        if (emitter != null) {
            try {
                String jsonData;
                if ("ERROR".equals(type)) {
                    jsonData = String.format("{\"type\":\"%s\",\"error\":\"%s\"}", 
                                           type, escapeJson(error));
                } else {
                    jsonData = String.format("{\"type\":\"%s\",\"content\":\"%s\"}", 
                                           type, escapeJson(content));
                }
                
                emitter.send(SseEmitter.event()
                        .name("chunk")
                        .data(jsonData));
                        
            } catch (Exception e) {
                log.error("SSE 이벤트 전송 오류: {}", e.getMessage());
                removeEmitter(streamId);
            }
        }
    }
    
    /**
     * 응답을 조각으로 나누어 스트리밍처럼 전송
     */
    private void simulateStreamingResponse(String streamId, String fullResponse) {
        try {
            String[] words = fullResponse.split(" ");
            StringBuilder currentContent = new StringBuilder();
            
            for (String word : words) {
                currentContent.append(word).append(" ");
                
                // 현재까지의 내용을 전송
                sendSSEEvent(streamId, "CONTENT", currentContent.toString().trim(), null);
                
                // 잠시 대기 (스트리밍 효과)
                Thread.sleep(50);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("스트리밍 시뮬레이션 중단됨");
        } catch (Exception e) {
            log.error("스트리밍 시뮬레이션 오류: {}", e.getMessage());
        }
    }
    
    /**
     * 스트리밍 완료 처리
     */
    private void handleStreamComplete(String streamId) {
        try {
            SseEmitter emitter = emitters.get(streamId);
            if (emitter != null) {
                emitter.complete();
            }
        } catch (Exception e) {
            log.error("스트리밍 완료 처리 실패: {}", e.getMessage());
        } finally {
            removeEmitter(streamId);
        }
    }
    
    /**
     * AI 응답 메시지 저장
     */
    private void saveAIMessage(ChatStreamContext context, String content) {
        try {
            Conversation conversation = conversationRepository.findById(context.getConversationId())
                    .orElseThrow(() -> new RuntimeException("대화를 찾을 수 없습니다."));
            
            Message aiMessage = Message.builder()
                    .conversation(conversation)
                    .role(Message.MessageRole.ASSISTANT)
                    .content(content)
                    .parentId(context.getUserMessageId())
                    .build();
            
            messageRepository.save(aiMessage);
            
            // 대화 업데이트 시간 갱신
            conversation.setUpdatedAt(LocalDateTime.now());
            conversationRepository.save(conversation);
            
            log.info("AI 응답 저장 완료 - 대화 ID: {}", context.getConversationId());
            
        } catch (Exception e) {
            log.error("AI 응답 저장 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 스트리밍 중단
     */
    public void stopStreaming(String streamId) {
        ChatStreamContext context = streamContexts.get(streamId);
        if (context != null && context.getAiStreamId() != null) {
            aiService.stopStreamingChat(context.getAiStreamId());
        }
        removeEmitter(streamId);
    }
    
    /**
     * Emitter 제거
     */
    public void removeEmitter(String streamId) {
        emitters.remove(streamId);
        streamContexts.remove(streamId);
        log.debug("Emitter 제거 - Stream ID: {}", streamId);
    }
    
    /**
     * JSON 문자열 이스케이프
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * 스트리밍 컨텍스트 클래스
     */
    public static class ChatStreamContext {
        private Long userId;
        private Long conversationId;
        private Long userMessageId;
        private String aiStreamId;
        private StringBuilder content;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getConversationId() { return conversationId; }
        public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
        
        public Long getUserMessageId() { return userMessageId; }
        public void setUserMessageId(Long userMessageId) { this.userMessageId = userMessageId; }
        
        public String getAiStreamId() { return aiStreamId; }
        public void setAiStreamId(String aiStreamId) { this.aiStreamId = aiStreamId; }
        
        public StringBuilder getContent() { return content; }
        public void setContent(StringBuilder content) { this.content = content; }
    }
} 