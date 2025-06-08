package com.clone.gpt.controller.prompt;

import com.clone.gpt.model.dto.request.prompt.PromptRequest;
import com.clone.gpt.model.dto.response.prompt.PromptResponse;
import com.clone.gpt.service.PromptService;
import com.clone.gpt.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PromptController {
    
    private final PromptService promptService;
    
    @PostMapping
    public ResponseEntity<PromptResponse> createPrompt(
            @Valid @RequestBody PromptRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("프롬프트 생성 요청 - 사용자 ID: {}, 제목: {}", userId, request.getTitle());
        PromptResponse response = promptService.createPrompt(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<PromptResponse>> getUserPrompts() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("사용자 프롬프트 목록 조회 - 사용자 ID: {}", userId);
        List<PromptResponse> prompts = promptService.getUserPrompts(userId);
        return ResponseEntity.ok(prompts);
    }
    
    @GetMapping("/{promptId}")
    public ResponseEntity<PromptResponse> getPrompt(
            @PathVariable Long promptId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("프롬프트 조회 - 사용자 ID: {}, 프롬프트 ID: {}", userId, promptId);
        PromptResponse prompt = promptService.getPrompt(userId, promptId);
        return ResponseEntity.ok(prompt);
    }
    
    @PutMapping("/{promptId}")
    public ResponseEntity<PromptResponse> updatePrompt(
            @PathVariable Long promptId,
            @Valid @RequestBody PromptRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("프롬프트 수정 - 사용자 ID: {}, 프롬프트 ID: {}, 새 제목: {}", userId, promptId, request.getTitle());
        PromptResponse response = promptService.updatePrompt(userId, promptId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{promptId}")
    public ResponseEntity<Map<String, String>> deletePrompt(
            @PathVariable Long promptId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("프롬프트 삭제 - 사용자 ID: {}, 프롬프트 ID: {}", userId, promptId);
        promptService.deletePrompt(userId, promptId);
        return ResponseEntity.ok(Map.of("message", "프롬프트가 삭제되었습니다."));
    }
} 