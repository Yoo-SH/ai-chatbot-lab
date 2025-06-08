package com.clone.gpt.model.dto.response.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.clone.gpt.model.entity.Message.MessageRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    
    private Long id;
    private Long parentId;
    private MessageRole role;
    private String content;
    private LocalDateTime createdAt;
} 