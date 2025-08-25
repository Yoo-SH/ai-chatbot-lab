package com.clone.gpt.model.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {
    
    private String response;
    private Map<String, Object> usage;
    private String model;
    private String timestamp;
} 