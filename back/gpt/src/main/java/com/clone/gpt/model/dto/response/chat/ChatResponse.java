package com.clone.gpt.model.dto.response.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    private Long messageId;
    private String content;
    private String role;
    private Long conversationId;
    private LocalDateTime timestamp;
    
    // 스트리밍 관련 필드
    private String streamId;
    private Boolean isStreaming;
    
    // 메타데이터
    private String status; // SUCCESS, ERROR, STREAMING
} 