package com.clone.gpt.controller.conversation;

import com.clone.gpt.model.dto.request.conversation.ConversationRequest;
import com.clone.gpt.model.dto.response.conversation.ConversationResponse;
import com.clone.gpt.service.ConversationService;
import com.clone.gpt.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConversationController {
    
    private final ConversationService conversationService;
    
    @PostMapping
    public ResponseEntity<ConversationResponse> createConversation(
            @Valid @RequestBody ConversationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("대화 생성 요청 - 사용자 ID: {}, 제목: {}", userId, request.getTitle());
        ConversationResponse response = conversationService.createConversation(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getUserConversations() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("사용자 대화 목록 조회 - 사용자 ID: {}", userId);
        List<ConversationResponse> conversations = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }
    
    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable Long conversationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("대화 조회 - 사용자 ID: {}, 대화 ID: {}", userId, conversationId);
        ConversationResponse conversation = conversationService.getConversation(userId, conversationId);
        return ResponseEntity.ok(conversation);
    }
    
    @PutMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> updateConversation(
            @PathVariable Long conversationId,
            @Valid @RequestBody ConversationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("대화 수정 - 사용자 ID: {}, 대화 ID: {}, 새 제목: {}", userId, conversationId, request.getTitle());
        ConversationResponse response = conversationService.updateConversation(userId, conversationId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Map<String, String>> deleteConversation(
            @PathVariable Long conversationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("대화 삭제 - 사용자 ID: {}, 대화 ID: {}", userId, conversationId);
        conversationService.deleteConversation(userId, conversationId);
        return ResponseEntity.ok(Map.of("message", "대화가 삭제되었습니다."));
    }
} 