package com.clone.gpt.model.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIStreamResponse {
    
    private String type;       // START, CONTENT, END, ERROR
    private String content;    // 실제 응답 내용
    private String error;      // 오류 메시지 (타입이 ERROR일 때)
} 