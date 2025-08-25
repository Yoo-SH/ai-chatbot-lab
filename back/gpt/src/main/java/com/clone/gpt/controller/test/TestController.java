package com.clone.gpt.controller.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> testRedis() {
        try {
            // Redis 연결 테스트
            String testKey = "test-connection";
            String testValue = "Redis 연결 테스트";
            
            // 저장
            redisTemplate.opsForValue().set(testKey, testValue);
            log.info("Redis 저장 시도: key={}, value={}", testKey, testValue);
            
            // 조회
            Object retrievedValue = redisTemplate.opsForValue().get(testKey);
            log.info("Redis 조회 결과: key={}, value={}", testKey, retrievedValue);
            
            // 삭제
            redisTemplate.delete(testKey);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Redis 연결 성공",
                "savedValue", testValue,
                "retrievedValue", retrievedValue != null ? retrievedValue.toString() : "null"
            ));
            
        } catch (Exception e) {
            log.error("Redis 연결 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Redis 연결 실패: " + e.getMessage()
            ));
        }
    }
}