package com.clone.gpt.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamChunk {
    
    public enum ChunkType {
        START,      // 스트리밍 시작
        CONTENT,    // 내용 청크
        END,        // 스트리밍 종료
        ERROR       // 에러
    }
    
    private ChunkType type;
    private String content;
    private String streamId;
    private Boolean isComplete;
    private LocalDateTime timestamp;
    
    // 에러 정보
    private String error;
    
    // 메타데이터
    private Long messageId;
    private Integer chunkIndex;
    private Integer totalLength;
} 