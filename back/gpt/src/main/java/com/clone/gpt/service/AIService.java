package com.clone.gpt.service;

import com.clone.gpt.model.dto.request.ai.AIRequest;
import com.clone.gpt.model.dto.response.ai.AIResponse;
import com.clone.gpt.model.dto.response.ai.AIStreamResponse;
import com.clone.gpt.model.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {
    
    private final RestTemplate restTemplate;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
    
    @Value("${ai.service.base-url:http://localhost:8000}")
    private String aiServiceBaseUrl;
    
    @Value("${ai.service.timeout:30}")
    private int timeoutSeconds;
    
    /**
     * 일반 채팅 - 전체 응답을 한 번에 받기
     */
    public AIResponse chat(String message, List<Message> conversationHistory, String systemPrompt) {
        try {
            log.info("AI 서비스 호출 - 메시지: {}", message.substring(0, Math.min(message.length(), 50)));
            
            // 요청 데이터 준비
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);
            requestBody.put("conversation_history", convertToAIFormat(conversationHistory));
            requestBody.put("system_prompt", systemPrompt != null ? systemPrompt : "당신은 도움이 되는 AI 어시스턴트입니다.");
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // AI 서비스 호출
            ResponseEntity<AIResponse> response = restTemplate.exchange(
                aiServiceBaseUrl + "/chat",
                HttpMethod.POST,
                entity,
                AIResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("AI 응답 성공");
                return response.getBody();
            } else {
                throw new RuntimeException("AI 서비스 응답 오류");
            }
            
        } catch (Exception e) {
            log.error("AI 서비스 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 서비스 연결에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 스트리밍 채팅 시작
     */
    public String startStreamingChat(String message, List<Message> conversationHistory, String systemPrompt) {
        try {
            log.info("AI 스트리밍 서비스 호출 - 메시지: {}", message.substring(0, Math.min(message.length(), 50)));
            
            // 요청 데이터 준비
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);
            requestBody.put("conversation_history", convertToAIFormat(conversationHistory));
            requestBody.put("system_prompt", systemPrompt != null ? systemPrompt : "당신은 도움이 되는 AI 어시스턴트입니다.");
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // AI 스트리밍 시작 호출
            ResponseEntity<Map> response = restTemplate.exchange(
                aiServiceBaseUrl + "/chat/stream",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String streamId = (String) response.getBody().get("stream_id");
                log.info("AI 스트리밍 시작 성공 - Stream ID: {}", streamId);
                return streamId;
            } else {
                throw new RuntimeException("AI 스트리밍 시작 실패");
            }
            
        } catch (Exception e) {
            log.error("AI 스트리밍 서비스 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("AI 스트리밍 서비스 연결에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 스트리밍 응답을 폴링 방식으로 받기
     */
    public void pollStreamingResponse(String aiStreamId, 
                                    Consumer<AIStreamResponse> onResponse,
                                    Consumer<Throwable> onError,
                                    Runnable onComplete) {
        
        CompletableFuture.runAsync(() -> {
            try {
                log.info("AI 스트리밍 폴링 시작 - Stream ID: {}", aiStreamId);
                
                String streamUrl = aiServiceBaseUrl + "/chat/stream/" + aiStreamId;
                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                // 폴링으로 스트리밍 데이터 받기
                StringBuilder buffer = new StringBuilder();
                long startTime = System.currentTimeMillis();
                boolean streamCompleted = false;
                
                while (!streamCompleted && (System.currentTimeMillis() - startTime) < (timeoutSeconds * 1000)) {
                    try {
                        ResponseEntity<String> response = restTemplate.exchange(
                            streamUrl, HttpMethod.GET, entity, String.class
                        );
                        
                        if (response.getStatusCode() == HttpStatus.OK) {
                            String responseBody = response.getBody();
                            if (responseBody != null && !responseBody.isEmpty()) {
                                
                                // SSE 응답을 라인별로 처리
                                String[] lines = responseBody.split("\n");
                                for (String line : lines) {
                                    if (line.startsWith("data: ")) {
                                        AIStreamResponse streamResponse = parseStreamingResponse(line);
                                        
                                        if ("END".equals(streamResponse.getType()) || "ERROR".equals(streamResponse.getType())) {
                                            streamCompleted = true;
                                        }
                                        
                                        onResponse.accept(streamResponse);
                                        
                                        if (streamCompleted) break;
                                    }
                                }
                            }
                        }
                        
                        // 잠시 대기 후 다시 폴링
                        if (!streamCompleted) {
                            Thread.sleep(100);
                        }
                        
                    } catch (Exception e) {
                        log.error("스트리밍 폴링 오류: {}", e.getMessage());
                        onError.accept(e);
                        return;
                    }
                }
                
                onComplete.run();
                log.info("AI 스트리밍 폴링 완료 - Stream ID: {}", aiStreamId);
                
            } catch (Exception e) {
                log.error("스트리밍 폴링 전체 오류: {}", e.getMessage(), e);
                onError.accept(e);
            }
        }, executor);
    }
    
    /**
     * 스트리밍 중단
     */
    public void stopStreamingChat(String aiStreamId) {
        try {
            log.info("AI 스트리밍 중단 - Stream ID: {}", aiStreamId);
            
            restTemplate.delete(aiServiceBaseUrl + "/chat/stream/" + aiStreamId);
            
        } catch (Exception e) {
            log.error("AI 스트리밍 중단 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * AI 서비스 헬스 체크
     */
    public boolean isHealthy() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                aiServiceBaseUrl + "/health",
                Map.class
            );
            
            return response.getStatusCode() == HttpStatus.OK && 
                   "healthy".equals(response.getBody().get("status"));
                   
        } catch (Exception e) {
            log.warn("AI 서비스 헬스 체크 실패: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 대화 기록을 AI 서비스 형태로 변환
     */
    private List<Map<String, String>> convertToAIFormat(List<Message> messages) {
        List<Map<String, String>> result = new ArrayList<>();
        
        if (messages != null) {
            for (Message message : messages) {
                Map<String, String> msgMap = new HashMap<>();
                msgMap.put("role", message.getRole().toString().toLowerCase());
                msgMap.put("content", message.getContent());
                result.add(msgMap);
            }
        }
        
        return result;
    }
    
    /**
     * 스트리밍 응답 파싱
     */
    private AIStreamResponse parseStreamingResponse(String rawData) {
        try {
            // SSE 형태의 데이터 파싱
            if (rawData == null || rawData.trim().isEmpty()) {
                return new AIStreamResponse("UNKNOWN", "", "빈 응답");
            }
            
            String jsonData = rawData;
            if (rawData.startsWith("data: ")) {
                jsonData = rawData.substring(6).trim();
            }
            
            if (jsonData.isEmpty()) {
                return new AIStreamResponse("UNKNOWN", "", "빈 데이터");
            }
            
            // JSON 파싱 시도
            try {
                // 간단한 JSON 파싱 (실제로는 Jackson ObjectMapper 사용 권장)
                if (jsonData.contains("\"type\":\"START\"")) {
                    String streamId = extractJsonValue(jsonData, "stream_id");
                    return new AIStreamResponse("START", "", null);
                } 
                else if (jsonData.contains("\"type\":\"CONTENT\"")) {
                    String content = extractJsonValue(jsonData, "content");
                    return new AIStreamResponse("CONTENT", content != null ? content : "", null);
                } 
                else if (jsonData.contains("\"type\":\"END\"")) {
                    String content = extractJsonValue(jsonData, "content");
                    return new AIStreamResponse("END", content != null ? content : "", null);
                } 
                else if (jsonData.contains("\"type\":\"ERROR\"")) {
                    String error = extractJsonValue(jsonData, "error");
                    return new AIStreamResponse("ERROR", "", error != null ? error : "알 수 없는 오류");
                }
                else {
                    log.warn("알 수 없는 스트리밍 응답 타입: {}", jsonData);
                    return new AIStreamResponse("UNKNOWN", jsonData, null);
                }
            } catch (Exception parseError) {
                log.error("JSON 파싱 오류: {}, 원본 데이터: {}", parseError.getMessage(), jsonData);
                return new AIStreamResponse("ERROR", "", "응답 파싱 오류: " + parseError.getMessage());
            }
            
        } catch (Exception e) {
            log.error("스트리밍 응답 파싱 전체 오류: {}, 원본: {}", e.getMessage(), rawData);
            return new AIStreamResponse("ERROR", "", "응답 처리 오류: " + e.getMessage());
        }
    }
    
    /**
     * 간단한 JSON 값 추출 (실제로는 Jackson 사용 권장)
     */
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) {
                return null;
            }
            
            startIndex += searchKey.length();
            int endIndex = json.indexOf("\"", startIndex);
            
            // 이스케이프된 따옴표 처리
            while (endIndex > 0 && json.charAt(endIndex - 1) == '\\') {
                endIndex = json.indexOf("\"", endIndex + 1);
            }
            
            if (endIndex == -1) {
                return null;
            }
            
            String value = json.substring(startIndex, endIndex);
            // 이스케이프 문자 복원
            return value.replace("\\\"", "\"")
                       .replace("\\n", "\n")
                       .replace("\\r", "\r")
                       .replace("\\t", "\t");
                       
        } catch (Exception e) {
            log.error("JSON 값 추출 오류 - key: {}, json: {}, error: {}", key, json, e.getMessage());
            return null;
        }
    }
} 