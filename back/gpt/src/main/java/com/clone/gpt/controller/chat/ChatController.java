package com.clone.gpt.controller.chat;

import com.clone.gpt.model.dto.request.chat.ChatRequest;
import com.clone.gpt.model.dto.response.chat.ChatResponse;
import com.clone.gpt.service.ChatService;
import com.clone.gpt.security.JwtTokenProvider;
import com.clone.gpt.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final ChatService chatService;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 일반 HTTP 방식 - 전체 응답을 한 번에 받기
     */
    @PostMapping
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("HTTP 채팅 요청 - 사용자 ID: {}, 메시지: {}", userId, request.getMessage());
        
        ChatResponse response = chatService.processMessage(userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * SSE 스트리밍 방식 - 스트리밍 시작
     */
    @PostMapping("/stream")
    public ResponseEntity<Map<String, String>> startStreamingChat(@Valid @RequestBody ChatRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("SSE 스트리밍 채팅 시작 - 사용자 ID: {}, 메시지: {}", userId, request.getMessage());
        
        String streamId = chatService.startStreaming(userId, request);
        return ResponseEntity.ok(Map.of("streamId", streamId));
    }
    
    /**
     * SSE 스트리밍 방식 - 스트리밍 응답 수신
     */
    @GetMapping(value = "/stream/{streamId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @PathVariable String streamId,
            @RequestParam(value = "token", required = false) String token) {
        
        log.info("SSE 스트리밍 연결 - Stream ID: {}", streamId);
        
        // 토큰이 제공된 경우 인증 확인
        if (token != null && !token.isEmpty()) {
            try {
                if (!jwtTokenProvider.validateToken(token)) {
                    log.warn("SSE 스트리밍 - 유효하지 않은 토큰: {}", streamId);
                    SseEmitter errorEmitter = new SseEmitter(1000L);
                    try {
                        errorEmitter.send(SseEmitter.event()
                                .name("error")
                                .data("{\"type\":\"ERROR\",\"error\":\"인증이 필요합니다\"}"));
                        errorEmitter.complete();
                    } catch (Exception e) {
                        // 무시
                    }
                    return errorEmitter;
                }
            } catch (Exception e) {
                log.error("SSE 토큰 검증 오류: {}", e.getMessage());
            }
        }
        
        SseEmitter emitter = new SseEmitter(60000L); // 60초 타임아웃
        
        try {
            // 연결 시작 알림
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("{\"type\":\"CONNECT\",\"streamId\":\"" + streamId + "\"}"));
        } catch (Exception e) {
            log.error("SSE 초기 연결 메시지 전송 실패: {}", e.getMessage());
        }
        
        // 스트리밍 서비스에 emitter 등록
        chatService.registerEmitter(streamId, emitter);
        
        // 완료/타임아웃 시 정리
        emitter.onCompletion(() -> {
            log.info("SSE 스트리밍 완료 - Stream ID: {}", streamId);
            chatService.removeEmitter(streamId);
        });
        
        emitter.onTimeout(() -> {
            log.warn("SSE 스트리밍 타임아웃 - Stream ID: {}", streamId);
            chatService.removeEmitter(streamId);
        });
        
        emitter.onError((throwable) -> {
            log.error("SSE 스트리밍 오류 - Stream ID: {}, 오류: {}", streamId, throwable.getMessage());
            chatService.removeEmitter(streamId);
        });
        
        return emitter;
    }
    
    /**
     * 진행 중인 스트리밍 중단
     */
    @DeleteMapping("/stream/{streamId}")
    public ResponseEntity<Map<String, String>> stopStreaming(@PathVariable String streamId) {
        log.info("SSE 스트리밍 중단 요청 - Stream ID: {}", streamId);
        
        chatService.stopStreaming(streamId);
        return ResponseEntity.ok(Map.of("message", "스트리밍이 중단되었습니다."));
    }
} 