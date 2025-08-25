package com.clone.gpt.model.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    @NotNull(message = "대화 ID는 필수입니다")
    private Long conversationId;
    
    @NotBlank(message = "메시지 내용은 필수입니다")
    private String message;
    
    private Long parentId;
    
    // 스트리밍 옵션
    @Builder.Default
    private Boolean streaming = false;
} 