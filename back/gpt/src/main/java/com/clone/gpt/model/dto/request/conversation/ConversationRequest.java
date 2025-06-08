package com.clone.gpt.model.dto.request.conversation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConversationRequest {
    
    @NotBlank(message = "제목은 필수입니다")
    private String title;
} 