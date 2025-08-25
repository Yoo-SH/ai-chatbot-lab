package com.clone.gpt.model.dto.request.prompt;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PromptRequest {
    
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다")
    private String content;
} 