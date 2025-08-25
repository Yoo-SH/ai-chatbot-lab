package com.clone.gpt.model.dto.request.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    
    private String message;
    private List<Map<String, String>> conversationHistory;
    private String systemPrompt;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Boolean stream;
} 