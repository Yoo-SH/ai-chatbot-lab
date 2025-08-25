package com.clone.gpt.controller.message;

import com.clone.gpt.model.dto.request.message.MessageRequest;
import com.clone.gpt.model.dto.response.message.MessageResponse;
import com.clone.gpt.service.MessageService;
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
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessageController {
    
    private final MessageService messageService;
    
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("메시지 전송 요청 - 사용자 ID: {}, 대화 ID: {}", userId, request.getConversationId());
        MessageResponse response = messageService.sendMessage(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getConversationMessages(
            @PathVariable Long conversationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("대화 메시지 조회 - 사용자 ID: {}, 대화 ID: {}", userId, conversationId);
        List<MessageResponse> messages = messageService.getConversationMessages(userId, conversationId);
        return ResponseEntity.ok(messages);
    }
    
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, String>> deleteMessage(
            @PathVariable Long messageId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("메시지 삭제 - 사용자 ID: {}, 메시지 ID: {}", userId, messageId);
        messageService.deleteMessage(userId, messageId);
        return ResponseEntity.ok(Map.of("message", "메시지가 삭제되었습니다."));
    }
    
    @PostMapping("/assistant")
    public ResponseEntity<MessageResponse> sendAssistantMessage(
            @RequestBody Map<String, Object> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long conversationId = Long.valueOf(request.get("conversationId").toString());
        String content = request.get("content").toString();
        Long parentId = request.get("parentId") != null ? 
            Long.valueOf(request.get("parentId").toString()) : null;
            
        log.info("AI 응답 메시지 저장 - 사용자 ID: {}, 대화 ID: {}", userId, conversationId);
        MessageResponse response = messageService.sendAssistantMessage(userId, conversationId, content, parentId);
        return ResponseEntity.ok(response);
    }
} 